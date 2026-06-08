const auth = require('../../utils/auth')
const approvalApi = require('../../api/approval')

const PAGE_SIZE = 20

Page({
  data: {
    tasks: [],
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
    this.loadTasks(true)
  },

  onShow() {
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadTasks(true)
    }
  },

  /** 加载待办任务列表 */
  async loadTasks(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })

    try {
      const tasks = await approvalApi.list({ page: page, size: PAGE_SIZE })
      const formatted = (tasks || []).map(t => ({
        ...t,
        createTimeStr: formatDateTime(t.createTime)
      }))

      if (reset) {
        this.setData({ tasks: formatted, page: page + 1, hasMore: formatted.length >= PAGE_SIZE })
      } else {
        this.setData({
          tasks: this.data.tasks.concat(formatted),
          page: page + 1,
          hasMore: formatted.length >= PAGE_SIZE
        })
      }
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadTasks(true)
    wx.stopPullDownRefresh()
  },

  /** 上拉加载更多 */
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadTasks(false)
  },

  /** 点击任务进入审批 */
  handleTaskTap(e) {
    const task = e.currentTarget.dataset.task
    const params = [
      'taskId=' + encodeURIComponent(task.taskId),
      'businessType=' + encodeURIComponent(task.businessType || ''),
      'businessId=' + (task.businessId || ''),
      'businessTitle=' + encodeURIComponent(task.businessTitle || ''),
      'businessSubtitle=' + encodeURIComponent(task.businessSubtitle || ''),
      'nodeName=' + encodeURIComponent(task.nodeName || '')
    ].join('&')
    wx.navigateTo({ url: '/pages/approval/action?' + params })
  }
})

/** 解析 ISO 日期时间为可读格式 */
function formatDateTime(dt) {
  if (!dt) return ''
  if (typeof dt === 'string') {
    return dt.substring(0, 16).replace('T', ' ')
  }
  return dt
}
