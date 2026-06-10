const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')
const uploadApi = require('../../api/upload')

Page({
  data: {
    isEdit: false, editId: null, title: '新建采购记录', submitting: false, error: '',
    availablePRs: [], selectedPRIds: [],
    showPRPicker: false,
    photos: [], form: { supplierName: '', supplierPhone: '', unitPrice: '', totalAmount: '', purchaseDate: '', expectedArrivalDate: '', remark: '' }
  },
  async onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    await this.loadAvailablePRs()
    if (options.id) {
      this.setData({ isEdit: true, editId: parseInt(options.id), title: '编辑采购记录' })
      this.loadDetail(options.id)
    }
  },
  async loadAvailablePRs() {
    try { const prs = await poApi.getAvailablePRs()
      this.setData({ availablePRs: (prs || []).map(p => ({ ...p, checked: false, displayLabel: (p.code||'') + ' ' + p.name + ' | ' + (p.projectName||'') + ' | ' + (p.specification||'-') + ' | ' + p.quantity + (p.unitLabel||'') })) })
    } catch { /* ignore */ }
  },
  async loadDetail(id) {
    try { const d = await poApi.detail(id)
      if (d.statusCode !== 0) { wx.showToast({ title: '当前状态不可编辑', icon: 'none' }); setTimeout(() => wx.navigateBack(), 1000); return }
      const selIds = (d.purchaseRequests || []).map(p => p.id)
      // 加载已有附件为缩略图
      const API_BASE = 'https://legwork-blush-vitally.ngrok-free.dev'
      const existingPhotos = (d.files || []).map(f => ({ filePath: API_BASE + f.url, fileId: f.id, uploading: false, isExisting: true }))
      this.setData({ photos: existingPhotos })
      // 将已关联的PR加入可选列表（available API 不会返回已关联的）
      const curPRs = this.data.availablePRs.map(p => ({ ...p, checked: selIds.indexOf(p.id) >= 0 }))
      const linkedPRs = (d.purchaseRequests || []).filter(p => !curPRs.some(c => c.id === p.id))
      const mergedPRs = curPRs.concat(linkedPRs.map(p => ({ id: p.id, code: p.code, name: p.name, projectName: p.projectName||'', specification: p.specification||'', quantity: p.quantityStr||'', unitLabel: '', arrivalDate: p.arrivalDate||'', checked: true, displayLabel: (p.code||'') + ' ' + p.name + ' | ' + (p.projectName||'') + ' | ' + (p.specification||'-') + ' | ' + p.quantityStr })))
      this.setData({ availablePRs: mergedPRs, selectedPRIds: selIds, form: { supplierName: d.supplierName || '', supplierPhone: d.supplierPhone || '', unitPrice: d.unitPrice != null ? String(d.unitPrice) : '', totalAmount: d.totalAmount != null ? String(d.totalAmount) : '', purchaseDate: d.purchaseDate || '', expectedArrivalDate: d.expectedArrivalDate || '', remark: d.remark || '' } })
    } catch (e) { wx.showToast({ title: e.message || '加载失败', icon: 'none' }); setTimeout(() => wx.navigateBack(), 1000) }
  },
  togglePRPicker() {
    // 打开面板时刷新勾选状态
    if (!this.data.showPRPicker) {
      const selIds = this.data.selectedPRIds
      const prs = this.data.availablePRs.map(p => ({ ...p, checked: selIds.indexOf(p.id) >= 0 }))
      this.setData({ availablePRs: prs })
    }
    this.setData({ showPRPicker: !this.data.showPRPicker })
  },
  togglePRCheck(e) {
    const id = e.currentTarget.dataset.id; const { selectedPRIds, availablePRs } = this.data
    const idx = selectedPRIds.indexOf(id)
    if (idx >= 0) { selectedPRIds.splice(idx, 1) } else { selectedPRIds.push(id) }
    // 同步更新列表中的 checked 状态
    const updatedPRs = availablePRs.map(p => {
      if (p.id === id) return { ...p, checked: idx < 0 }
      return p
    })
    this.setData({ selectedPRIds, availablePRs: updatedPRs })
  },
  getSelectedPRs() { return this.data.availablePRs.filter(p => this.data.selectedPRIds.includes(p.id)) },
  onInput(e) { this.setData({ ['form.' + e.currentTarget.dataset.field]: e.detail.value }) },
  onDateChange(e) { this.setData({ 'form.purchaseDate': e.detail.value }) },
  onArrivalDateChange(e) { this.setData({ 'form.expectedArrivalDate': e.detail.value }) },

  chooseImage() {
    const remaining = 9 - this.data.photos.length; if (remaining <= 0) { wx.showToast({ title: '最多9张', icon: 'none' }); return }
    wx.showActionSheet({ itemList: ['拍照', '从相册选择'], success: (res) => {
      if (res.cancel) return
      wx.chooseImage({ count: remaining, sizeType: ['compressed'], sourceType: res.tapIndex === 0 ? ['camera'] : ['album'], success: (cr) => {
        const np = cr.tempFilePaths.map(fp => ({ filePath: fp, uploading: false, fileId: null }))
        this.setData({ photos: this.data.photos.concat(np) }); this.uploadPhotos()
      }})
    }})
  },
  async uploadPhotos() {
    for (let i = 0; i < this.data.photos.length; i++) {
      const p = this.data.photos[i]; if (p.fileId || p.uploading) continue
      this.setData({ ['photos[' + i + '].uploading']: true })
      try { const fid = await uploadApi.uploadFile(p.filePath, 'purchase_order'); this.setData({ ['photos[' + i + '].fileId']: fid, ['photos[' + i + '].uploading']: false }) }
      catch { this.setData({ ['photos[' + i + '].uploading']: false }); wx.showToast({ title: '第' + (i+1) + '张上传失败', icon: 'none' }) }
    }
  },
  removePhoto(e) { const idx = e.currentTarget.dataset.index; this.data.photos.splice(idx, 1); this.setData({ photos: this.data.photos }) },
  validate() {
    const f = this.data.form
    if (!this.data.isEdit && this.data.selectedPRIds.length === 0) { this.setData({ error: '请至少选择一个采购申请' }); return false }
    if (!f.supplierName.trim()) { this.setData({ error: '请输入供应商名称' }); return false }
    if (!f.totalAmount || parseFloat(f.totalAmount) <= 0) { this.setData({ error: '请输入有效总金额' }); return false }
    if (!f.purchaseDate) { this.setData({ error: '请选择采购日期' }); return false }
    if (!this.data.isEdit && this.data.photos.filter(p => p.fileId).length === 0) { this.setData({ error: '至少需要上传一个附件' }); return false }
    return true
  },
  async handleSubmit() {
    this.setData({ error: '' }); if (!this.validate()) return; this.setData({ submitting: true })
    const f = this.data.form
    const payload = { supplierName: f.supplierName.trim(), supplierPhone: f.supplierPhone.trim() || null, unitPrice: f.unitPrice ? parseFloat(f.unitPrice) : null, totalAmount: parseFloat(f.totalAmount), purchaseDate: f.purchaseDate, expectedArrivalDate: f.expectedArrivalDate || null, remark: f.remark.trim() || null }
    payload.purchaseRequestIds = this.data.selectedPRIds
    const newFileIds = this.data.photos.filter(p => p.fileId && !p.isExisting).map(p => p.fileId)
    if (newFileIds.length > 0) { payload.fileIds = newFileIds }
    try {
      if (this.data.isEdit) { await poApi.update(this.data.editId, payload) }
      else { await poApi.create(payload) }
      wx.showToast({ title: this.data.isEdit ? '修改成功' : '创建成功', icon: 'success' })
      if (this.data.isEdit) {
        const pages = getCurrentPages(); const prev = pages[pages.length - 2]
        if (prev) prev.setData({ needRefresh: true })
        setTimeout(() => { wx.navigateBack() }, 1000)
      } else {
        const pages = getCurrentPages(); const prev = pages[pages.length - 2]; if (prev) prev.setData({ needRefresh: true })
        setTimeout(() => wx.navigateBack(), 1000)
      }
    } catch (e) { wx.showToast({ title: e.message || '提交失败', icon: 'none' }) }
    finally { this.setData({ submitting: false }) }
  }
})
