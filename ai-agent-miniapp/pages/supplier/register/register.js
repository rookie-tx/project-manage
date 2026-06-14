const auth = require('../../../utils/auth')
const supplierApi = require('../../../api/supplier')

Page({
  data: {
    form: { companyName: '', licenseNo: '', contactPerson: '', contactPhone: '', address: '' },
    agreed: false,
    submitted: false,
    loading: false,
    error: ''
  },

  onLoad() {
    if (auth.isLoggedIn()) {
      wx.switchTab({ url: '/pages/dashboard/dashboard' })
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  toggleAgree() {
    this.setData({ agreed: !this.data.agreed })
  },

  goLogin() {
    wx.navigateBack()
  },

  validate() {
    this.setData({ submitted: true, error: '' })
    const f = this.data.form
    if (!f.companyName.trim()) { this.setData({ error: '请填写公司名称' }); return false }
    if (!f.contactPerson.trim()) { this.setData({ error: '请填写联系人姓名' }); return false }
    if (!f.contactPhone.trim() || f.contactPhone.length < 11) { this.setData({ error: '请填写正确的手机号码' }); return false }
    if (!this.data.agreed) { this.setData({ error: '请阅读并同意服务协议' }); return false }
    return true
  },

  async handleSubmit() {
    if (!this.validate()) return
    this.setData({ loading: true, error: '' })

    try {
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({ success: resolve, fail: reject })
      })

      if (!loginRes.code) {
        this.setData({ error: '获取微信授权失败，请重试', loading: false })
        return
      }

      const f = this.data.form
      const data = await supplierApi.register({
        code: loginRes.code,
        companyName: f.companyName.trim(),
        licenseNo: f.licenseNo.trim() || undefined,
        contactPerson: f.contactPerson.trim(),
        contactPhone: f.contactPhone.trim(),
        address: f.address.trim() || undefined
      })

      auth.setAuth(data.token, {
        id: data.user.id,
        username: data.user.username,
        name: data.user.name,
        permissions: data.user.permissions
      })

      wx.showToast({ title: '入驻成功', icon: 'success' })
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/dashboard/dashboard' })
      }, 1200)
    } catch (e) {
      this.setData({ error: e.message || '注册失败，请稍后重试' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
