const auth = require('./utils/auth')

App({
  onLaunch() {
    // 检查登录状态，已登录则跳转 Dashboard
    if (auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/dashboard/dashboard' })
    }
  },

  globalData: {
    userInfo: null
  }
})
