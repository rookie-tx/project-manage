const auth = require('../../utils/auth')
const purchaseApi = require('../../api/purchase')
const approvalApi = require('../../api/approval')

const PAGE_SIZE = 20

const STATUS_MAP = {
  0: '待提交', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已取消'
}

const UNIT_MAP = {
  0: '个', 1: '台', 2: '套', 3: '米', 4: '吨',
  5: '立方米', 6: '卷', 7: '桶', 8: '箱', 9: '其他'
}

Page({
  data: {
    canCreate: false,
    projects: [],
    projectNames: ['全部项目'],
    selectedProjectIdx: 0,
    items: [],
    loading: false,
    hasMore: true,
    page: 1,
    refreshing: false,
    needRefresh: false
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ canCreate: auth.hasPermission('pr:create') })
    this.loadProjects()
    this.loadItems(true)
  },

  onShow() {
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadItems(true)
    }
  },

  async loadProjects() {
    try {
      const projects = await purchaseApi.getProjects()
      this.setData({
        projects: projects || [],
        projectNames: ['全部项目'].concat((projects || []).map(p => p.name))
      })
    } catch { /* ignore */ }
  },

  async loadItems(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })

    try {
      const params = { page: page, size: PAGE_SIZE }
      const { selectedProjectIdx, projects } = this.data
      if (selectedProjectIdx > 0 && projects[selectedProjectIdx - 1]) {
        params.projectId = projects[selectedProjectIdx - 1].id
      }

      const result = await purchaseApi.list(params)
      const items = (result.items || []).map(r => ({
        ...r,
        arrivalDateStr: formatDate(r.arrivalDate),
        quantityStr: r.quantity + (UNIT_MAP[r.unitCode] || ''),
        statusLabel: STATUS_MAP[r.statusCode] || '未知',
        isPending: r.statusCode === 0
      }))

      if (reset) {
        this.setData({ items: items, page: page + 1, hasMore: items.length >= PAGE_SIZE })
      } else {
        this.setData({
          items: this.data.items.concat(items),
          page: page + 1,
          hasMore: items.length >= PAGE_SIZE
        })
      }
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadItems(true)
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadItems(false)
  },

  onProjectChange(e) {
    this.setData({ selectedProjectIdx: parseInt(e.detail.value) })
    this.loadItems(true)
  },

  handleView(e) { this.navigateToDetail(e) },
  handleEdit(e) { this.navigateToForm(e, true) },

  navigateToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/purchase/detail?id=' + id })
  },

  navigateToForm(e, isEdit) {
    const id = e.currentTarget.dataset.id
    const url = isEdit ? '/pages/purchase/form?id=' + id : '/pages/purchase/form'
    wx.navigateTo({ url: url })
  },

  handleCreate() {
    wx.navigateTo({ url: '/pages/purchase/form' })
  },

  async handleSubmitApproval(e) {
    const id = e.currentTarget.dataset.id
    // 直接跳转到详情页，详情页有提交审批功能
    wx.navigateTo({ url: '/pages/purchase/detail?id=' + id + '&showSubmit=1' })
  },

  async handleDelete(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '删除后数据不可恢复，确定要删除此采购申请吗？',
      confirmText: '确认删除',
      confirmColor: '#ff4d4f',
      cancelText: '取消',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await purchaseApi.del(id)
          wx.showToast({ title: '已删除', icon: 'success' })
          this.loadItems(true)
        } catch (err) {
          wx.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    })
  }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  if (typeof dateStr === 'string' && dateStr.length >= 10) return dateStr.substring(0, 10)
  return dateStr
}
