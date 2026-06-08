const auth = require('../../utils/auth')
const wasteApi = require('../../api/constructionWaste')
const uploadApi = require('../../api/upload')

Page({
  data: {
    // 项目选择器
    projects: [],
    projectNames: [],
    selectedProjectIdx: -1,

    // 表单字段
    form: {
      projectId: null,
      wasteType: '',
      quantity: '',
      unit: '',
      vehiclePlate: '',
      driverName: '',
      destination: '',
      removalDate: '',
      remark: ''
    },

    // 照片
    photos: [],        // [{ filePath, fileName, uploading, fileId }]
    submitting: false,
    error: ''
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadProjects()
  },

  /** 加载项目列表 */
  async loadProjects() {
    try {
      const projects = await wasteApi.getProjects()
      this.setData({
        projects: projects || [],
        projectNames: (projects || []).map(p => p.name)
      })
    } catch {
      wx.showToast({ title: '加载项目列表失败', icon: 'none' })
    }
  },

  /** 项目选择 */
  onProjectChange(e) {
    const idx = parseInt(e.detail.value)
    const project = this.data.projects[idx]
    this.setData({
      selectedProjectIdx: idx,
      'form.projectId': project ? project.id : null
    })
  },

  /** 表单输入绑定 */
  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  /** 日期选择 */
  onDateChange(e) {
    this.setData({ 'form.removalDate': e.detail.value })
  },

  /** 选择照片（拍照或相册） */
  chooseImage() {
    const remaining = 9 - this.data.photos.length
    if (remaining <= 0) {
      wx.showToast({ title: '最多上传9张照片', icon: 'none' })
      return
    }

    wx.showActionSheet({
      itemList: ['拍照', '从相册选择'],
      success: (res) => {
        if (res.cancel) return
        const sourceType = res.tapIndex === 0 ? ['camera'] : ['album']
        wx.chooseImage({
          count: remaining,
          sizeType: ['compressed'],
          sourceType: sourceType,
          success: (chooseRes) => {
            const newPhotos = chooseRes.tempFilePaths.map(fp => ({
              filePath: fp,
              fileName: fp.substring(fp.lastIndexOf('/') + 1),
              uploading: false,
              fileId: null
            }))
            this.setData({ photos: this.data.photos.concat(newPhotos) })
            // 自动开始上传
            this.uploadPhotos()
          },
          fail: (err) => {
            if (err.errMsg && err.errMsg.includes('cancel')) return
            wx.showToast({ title: '选择照片失败', icon: 'none' })
          }
        })
      }
    })
  },

  /** 上传所有未上传的照片 */
  async uploadPhotos() {
    const photos = this.data.photos
    for (let i = 0; i < photos.length; i++) {
      const photo = photos[i]
      if (photo.fileId || photo.uploading) continue

      this.setData({ ['photos[' + i + '].uploading']: true })

      try {
        const fileId = await uploadApi.uploadFile(photo.filePath, 'construction_waste')
        this.setData({
          ['photos[' + i + '].fileId']: fileId,
          ['photos[' + i + '].uploading']: false
        })
      } catch (e) {
        this.setData({ ['photos[' + i + '].uploading']: false })
        wx.showToast({ title: '第' + (i + 1) + '张照片上传失败，请重试', icon: 'none' })
      }
    }
  },

  /** 移除照片 */
  removePhoto(e) {
    const idx = e.currentTarget.dataset.index
    const photos = this.data.photos
    photos.splice(idx, 1)
    this.setData({ photos: photos })
  },

  /** 表单校验 */
  validate() {
    const { form, photos } = this.data
    if (!form.projectId) { this.setData({ error: '请选择项目' }); return false }
    if (!form.wasteType.trim()) { this.setData({ error: '请输入废渣种类' }); return false }
    if (!form.quantity || parseFloat(form.quantity) <= 0) { this.setData({ error: '请输入有效数量' }); return false }
    if (!form.unit.trim()) { this.setData({ error: '请输入单位' }); return false }
    if (!form.removalDate) { this.setData({ error: '请选择运出日期' }); return false }

    // 检查是否有照片正在上传
    if (photos.some(p => p.uploading)) { this.setData({ error: '照片上传中，请稍候' }); return false }

    // 检查是否至少有一张已上传的照片
    const uploadedIds = photos.filter(p => p.fileId).map(p => p.fileId)
    if (uploadedIds.length === 0) { this.setData({ error: '至少需要上传一张现场照片' }); return false }

    return true
  },

  /** 提交表单 */
  async handleSubmit() {
    this.setData({ error: '' })
    if (!this.validate()) return

    this.setData({ submitting: true })

    try {
      const { form, photos } = this.data
      const fileIds = photos.filter(p => p.fileId).map(p => p.fileId)

      const payload = {
        projectId: form.projectId,
        wasteType: form.wasteType.trim(),
        quantity: parseFloat(form.quantity),
        unit: form.unit.trim(),
        vehiclePlate: form.vehiclePlate.trim() || null,
        driverName: form.driverName.trim() || null,
        destination: form.destination.trim() || null,
        removalDate: form.removalDate,
        remark: form.remark.trim() || null,
        fileIds: fileIds
      }

      await wasteApi.create(payload)
      wx.showToast({ title: '创建成功', icon: 'success' })
      // 通知列表页刷新
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage) {
        prevPage.setData({ needRefresh: true })
      }
      setTimeout(() => { wx.navigateBack() }, 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '创建失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
