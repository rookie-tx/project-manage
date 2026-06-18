const auth = require('../../utils/auth')
const approvalApi = require('../../api/approval')

/**
 * 所有可用功能模块的注册表。
 * 每个模块声明所需的权限；无权限的模块自动隐藏。
 * 当用户拥有 supplier:profile 权限时，不展示其他业务模块（供应商专属视图）。
 */
const ALL_FEATURES = [
  { key: 'supplier', icon: '🏢', name: '我的供应商', tip: '查看资料', url: '/pages/supplier/profile/profile', color: '#eef2ff', permission: 'supplier:profile' },
  { key: 'quotationPending', icon: '📋', name: '待报价', tip: '物料报价', url: '/pages/quotation/pending', color: '#fef3c7', permission: 'supplier:profile' },
  { key: 'waste',    icon: '📷', name: '废渣管理',   tip: '记录运出', url: '/pages/waste/list',             color: '#eff6ff', permission: 'waste:read' },
  { key: 'purchase', icon: '📦', name: '采购申请',   tip: '提交申请', url: '/pages/purchase/list',           color: '#fef3c7', permission: 'pr:read' },
  { key: 'purchaseSummary', icon: '📋', name: '采购汇总', tip: '全部记录', url: '/pages/purchase-order/list',      color: '#fef3c7', permission: 'purchase:read' },
  { key: 'pendingPurchase', icon: '📋', name: '待采购',   tip: '待处理',   url: '/pages/purchase-order/pending',   color: '#fef3c7', permission: 'purchase:pending:read' },
  { key: 'purchasedPurchase', icon: '📋', name: '已采购', tip: '待入库',   url: '/pages/purchase-order/purchased', color: '#fef3c7', permission: 'purchase:purchased:read' },
  { key: 'arrivedPurchase', icon: '📋', name: '已到货',   tip: '已完成',   url: '/pages/purchase-order/arrived',  color: '#fef3c7', permission: 'purchase:arrived:read' },
  { key: 'material', icon: '🔧', name: '物料管理',   tip: '管理物料', url: '/pages/material/list',           color: '#dcfce7', permission: 'material:read' },
  { key: 'checkin',  icon: '⏱️', name: '考勤打卡',   tip: '上班签到', url: '/pages/checkin/checkin',         color: '#dbeafe', permission: 'attendance:clock-in' },
  { key: 'approval', icon: '✅', name: '待办审批',   tip: '处理任务', url: '/pages/approval/list',           color: '#fce7f3', permission: 'task:read' },
  { key: 'project',  icon: '🏗️', name: '项目管理',   tip: '查看项目', url: '/pages/project/list',            color: '#f3f4f6', permission: 'project:read' }
]

Page({
  data: {
    username: '',
    displayName: '',
    greetingText: '',
    checkedIn: false,
    approvalCount: 0,
    features: [],
    showApprovalOverview: true,
    showCheckinOverview: true
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = auth.getUser()
    const permSet = new Set(user.permissions || [])

    // 按权限过滤模块
    const visibleFeatures = ALL_FEATURES.filter(f => permSet.has(f.permission))
    // 供应商用户仅展示供应商相关模块
    const isSupplier = permSet.has('supplier:profile')
    // const features = isSupplier
    //   ? visibleFeatures.filter(f => f.key === 'supplier')
    //   : visibleFeatures

    const features = visibleFeatures

    this.setData({
      username: user ? (user.name || user.username) : '',
      displayName: user ? (user.name || user.username) : '未知',
      greetingText: getGreeting(),
      features,
      showApprovalOverview: permSet.has('task:read'),
      showCheckinOverview: permSet.has('attendance:clock-in')
    })

    // 只有可查看审批时才去拉取待办数
    if (permSet.has('task:read')) {
      this.loadApprovalCount()
    }
  },

  onShow() {
    const user = auth.getUser()
    if (user && new Set(user.permissions || []).has('task:read')) {
      this.loadApprovalCount()
    }
  },

  async loadApprovalCount() {
    try {
      const result = await approvalApi.count()
      const count = (result && typeof result.count === 'number') ? result.count : Number(result && result.count || 0)
      this.setData({ approvalCount: count })
    } catch (e) {
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
