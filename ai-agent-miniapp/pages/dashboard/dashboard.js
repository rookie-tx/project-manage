const auth = require('../../utils/auth')
const approvalApi = require('../../api/approval')

Page({
  data: {
    username: '',
    approvalCount: 0,
    features: [
      { icon: '📋', name: '项目管理', tip: '即将上线' },
      { icon: '📷', name: '废渣管理', tip: '点击进入', url: '/pages/waste/list' },
      { icon: '⏱️', name: '考勤打卡', tip: '点击进入', url: '/pages/checkin/checkin' },
      { icon: '✅', name: '待办审批', tip: '点击进入', url: '/pages/approval/list', badge: true }
    ]
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = auth.getUser()
    this.setData({ username: user ? user.username : '未知' })
    this.loadApprovalCount()
  },

  onShow() {
    this.loadApprovalCount()
  },

  /** 加载待办审批数量 */
  async loadApprovalCount() {
    try {
      const result = await approvalApi.count()
      this.setData({ approvalCount: result ? result.count : 0 })
    } catch {
      // 计数失败时不显示角标
      this.setData({ approvalCount: 0 })
    }
  },

  handleFeatureTap(e) {
    const idx = e.currentTarget.dataset.index
    const feature = this.data.features[idx]
    if (feature && feature.url) {
      wx.navigateTo({ url: feature.url })
    }
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          auth.logout()
        }
      }
    })
  }
})
