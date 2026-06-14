const auth = require('../../../utils/auth')
const supplierApi = require('../../../api/supplier')

Page({
  data: {
    supplier: null,
    editing: false,
    form: { contactPerson: '', contactPhone: '', address: '' },
    loading: false,
    submitting: false,
    error: ''
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (!auth.hasPermission('supplier:profile')) {
      wx.showToast({ title: '仅供应商可访问', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
      return
    }
    this.loadProfile()
  },

  async loadProfile() {
    this.setData({ loading: true, error: '' })
    try {
      const data = await supplierApi.getProfile()
      this.setData({ supplier: data })
    } catch (e) {
      this.setData({ error: e.message || '加载失败' })
    } finally {
      this.setData({ loading: false })
    }
  },

  startEdit() {
    const s = this.data.supplier
    this.setData({
      editing: true,
      form: {
        contactPerson: s.contactPerson || '',
        contactPhone: s.contactPhone || '',
        address: s.address || ''
      }
    })
  },

  cancelEdit() {
    this.setData({ editing: false, error: '' })
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  validate() {
    const f = this.data.form
    if (!f.contactPerson.trim()) { this.setData({ error: '请输入联系人' }); return false }
    if (!f.contactPhone.trim()) { this.setData({ error: '请输入联系电话' }); return false }
    return true
  },

  async handleSave() {
    this.setData({ error: '' })
    if (!this.validate()) return
    this.setData({ submitting: true })
    try {
      const f = this.data.form
      await supplierApi.updateProfile({
        contactPerson: f.contactPerson.trim(),
        contactPhone: f.contactPhone.trim(),
        address: f.address.trim() || undefined
      })
      this.setData({ editing: false, submitting: false })
      this.loadProfile()
      wx.showToast({ title: '更新成功', icon: 'success' })
    } catch (e) {
      this.setData({ error: e.message || '更新失败', submitting: false })
    }
  }
})
