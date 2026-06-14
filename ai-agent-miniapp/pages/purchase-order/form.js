const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')
const uploadApi = require('../../api/upload')

Page({
  data: {
    editId: null, title: '完善采购信息', submitting: false, error: '',
    photos: [],
    form: {
      supplierName: '', supplierPhone: '', unitPrice: '',
      totalAmount: '', purchaseDate: '', expectedArrivalDate: '', remark: ''
    }
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    if (options.id) {
      this.setData({ editId: parseInt(options.id) })
      this.loadDetail(options.id)
    }
  },

  async loadDetail(id) {
    try {
      const d = await poApi.detail(id)
      if (d.statusCode !== 0) {
        wx.showToast({ title: '当前状态不可编辑', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1000)
        return
      }
      // 加载已有附件
      const API_BASE = 'https://legwork-blush-vitally.ngrok-free.dev'
      const existingPhotos = (d.files || []).map(f => ({
        filePath: API_BASE + f.url, fileId: f.id, uploading: false, isExisting: true
      }))
      this.setData({
        photos: existingPhotos,
        form: {
          supplierName: d.supplierName || '',
          supplierPhone: d.supplierPhone || '',
          unitPrice: d.unitPrice != null ? String(d.unitPrice) : '',
          totalAmount: d.totalAmount != null ? String(d.totalAmount) : '',
          purchaseDate: d.purchaseDate || '',
          expectedArrivalDate: d.expectedArrivalDate || '',
          remark: d.remark || ''
        }
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
    }
  },

  onInput(e) { this.setData({ ['form.' + e.currentTarget.dataset.field]: e.detail.value }) },
  onDateChange(e) { this.setData({ 'form.purchaseDate': e.detail.value }) },
  onArrivalDateChange(e) { this.setData({ 'form.expectedArrivalDate': e.detail.value }) },

  // 拍照/相册
  chooseImage() {
    const remaining = 9 - this.data.photos.length
    if (remaining <= 0) { wx.showToast({ title: '最多9张', icon: 'none' }); return }
    wx.showActionSheet({
      itemList: ['拍照', '从相册选择'],
      success: (res) => {
        if (res.cancel) return
        wx.chooseImage({
          count: remaining, sizeType: ['compressed'],
          sourceType: res.tapIndex === 0 ? ['camera'] : ['album'],
          success: (cr) => {
            const np = cr.tempFilePaths.map(fp => ({ filePath: fp, uploading: false, fileId: null }))
            this.setData({ photos: this.data.photos.concat(np) })
            this.uploadPhotos()
          }
        })
      }
    })
  },

  async uploadPhotos() {
    for (let i = 0; i < this.data.photos.length; i++) {
      const p = this.data.photos[i]
      if (p.fileId || p.uploading) continue
      this.setData({ ['photos[' + i + '].uploading']: true })
      try {
        const fid = await uploadApi.uploadFile(p.filePath, 'purchase_order')
        this.setData({ ['photos[' + i + '].fileId']: fid, ['photos[' + i + '].uploading']: false })
      } catch (e) {
        this.setData({ ['photos[' + i + '].uploading']: false })
        wx.showToast({ title: '第' + (i + 1) + '张上传失败', icon: 'none' })
      }
    }
  },

  removePhoto(e) {
    const idx = e.currentTarget.dataset.index
    this.data.photos.splice(idx, 1)
    this.setData({ photos: this.data.photos })
  },

  validate() {
    const f = this.data.form
    if (!f.supplierName.trim()) { this.setData({ error: '请输入供应商名称' }); return false }
    if (!f.totalAmount || parseFloat(f.totalAmount) <= 0) { this.setData({ error: '请输入有效总金额' }); return false }
    if (!f.purchaseDate) { this.setData({ error: '请选择采购日期' }); return false }
    return true
  },

  async handleSubmit() {
    this.setData({ error: '' })
    if (!this.validate()) return
    this.setData({ submitting: true })

    const f = this.data.form
    const payload = {
      supplierName: f.supplierName.trim(),
      supplierPhone: f.supplierPhone.trim() || null,
      unitPrice: f.unitPrice ? parseFloat(f.unitPrice) : null,
      totalAmount: parseFloat(f.totalAmount),
      purchaseDate: f.purchaseDate,
      expectedArrivalDate: f.expectedArrivalDate || null,
      remark: f.remark.trim() || null
    }

    const newFileIds = this.data.photos.filter(p => p.fileId && !p.isExisting).map(p => p.fileId)
    if (newFileIds.length > 0) { payload.fileIds = newFileIds }

    try {
      await poApi.update(this.data.editId, payload)
      // 补填供应商信息后直接流转到已采购
      await poApi.changeStatus(this.data.editId, 1)
      wx.showToast({ title: '采购完成', icon: 'success' })
      const pages = getCurrentPages()
      const prev = pages[pages.length - 2]
      if (prev) prev.setData({ needRefresh: true })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
