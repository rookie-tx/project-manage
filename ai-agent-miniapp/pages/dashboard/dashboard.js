const auth = require('../../utils/auth')
const approvalApi = require('../../api/approval')

Page({
  data: {
    username: '',
    greetingText: '',
    checkedIn: false,
    approvalCount: 0,
    features: [
      { icon: '📷', name: '废渣管理', tip: '点击使用', active: true, url: '/pages/waste/list', color: '#eff6ff' },
      { icon: '📦', name: '采购申请', tip: '点击使用', active: true, url: '/pages/purchase/list', color: '#fef3c7' },
      { icon: '⏱️', name: '考勤打卡', tip: '点击使用', active: true, url: '/pages/checkin/checkin', color: '#dbeafe' },
      { icon: '✅', name: '待办审批', tip: '点击使用', active: true, url: '/pages/approval/list', badge: true, color: '#fce7f3' },
      { icon: '📋', name: '项目管理', tip: '点击使用', active: true, url: '/pages/project/list', color: '#f3f4f6' },
      { icon: '📦', name: '采购管理', tip: '点击使用', active: true, url: '/pages/purchase-order/list', color: '#fef3c7' }
    ]
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = auth.getUser()
    this.setData({
      username: user ? (user.name || user.username) : '未知',
      greetingText: getGreeting()
    })
    this.loadApprovalCount()
  },

  onShow() {
    this.loadApprovalCount()
  },

  async loadApprovalCount() {
    try {
      const result = await approvalApi.count()
      const count = (result && typeof result.count === 'number') ? result.count : Number(result && result.count || 0)
      this.setData({ approvalCount: count })
    } catch {
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

  handleOverviewTap(e) {
    const type = e.currentTarget.dataset.type
    if (type === 'checkin') {
      wx.navigateTo({ url: '/pages/checkin/checkin' })
    } else if (type === 'approval') {
      wx.navigateTo({ url: '/pages/approval/list' })
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

function getGreeting() {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，注意休息'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}
