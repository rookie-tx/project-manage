const authApi = require('../../api/auth')
const auth = require('../../utils/auth')

Page({
  data: {
    showBindForm: false,
    username: '',
    password: '',
    loginError: '',
    bindError: '',
    loading: false
  },

  onLoad() {
    // 已登录直接跳转
    if (auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/dashboard/dashboard' })
    }
  },

  // 微信一键登录
  handleWechatLogin() {
    this.setData({ loginError: '', loading: true })
    wx.login({
      success: async (loginRes) => {
        if (!loginRes.code) {
          this.setData({ loginError: '获取微信授权失败', loading: false })
          return
        }
        try {
          const data = await authApi.wechatLogin(loginRes.code)
          if (data.needBind) {
            this.setData({ showBindForm: true, loading: false })
          } else {
            auth.setAuth(data.token, data.user)
            wx.reLaunch({ url: '/pages/dashboard/dashboard' })
          }
        } catch (e) {
          this.setData({ loginError: e.message || '登录失败', loading: false })
        }
      },
      fail: () => {
        this.setData({ loginError: '微信登录失败', loading: false })
      }
    })
  },

  // 绑定已有账号
  handleBind() {
    const { username, password } = this.data
    if (!username.trim()) {
      this.setData({ bindError: '请输入用户名' })
      return
    }
    if (!password) {
      this.setData({ bindError: '请输入密码' })
      return
    }

    this.setData({ bindError: '', loading: true })
    wx.login({
      success: async (loginRes) => {
        if (!loginRes.code) {
          this.setData({ bindError: '获取微信授权失败', loading: false })
          return
        }
        try {
          const data = await authApi.wechatBind(loginRes.code, username.trim(), password)
          auth.setAuth(data.token, data.user)
          wx.reLaunch({ url: '/pages/dashboard/dashboard' })
        } catch (e) {
          this.setData({ bindError: e.message || '绑定失败', loading: false })
        }
      },
      fail: () => {
        this.setData({ bindError: '微信登录失败', loading: false })
      }
    })
  },

  // 返回微信登录
  goBack() {
    this.setData({ showBindForm: false, username: '', password: '', bindError: '' })
  },

  // 输入绑定
  onUsernameInput(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) }
})
