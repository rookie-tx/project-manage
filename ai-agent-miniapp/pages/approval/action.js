const auth = require('../../utils/auth')
const approvalApi = require('../../api/approval')

Page({
  data: {
    // 任务信息（从 URL 参数解析）
    taskId: '',
    businessType: '',
    businessId: null,
    businessTitle: '',
    businessSubtitle: '',
    nodeName: '',
    createTime: '',

    // 审批历史
    history: [],
    historyLoading: false,

    // 权限
    canComplete: false,

    // 操作状态
    comment: '',
    submitting: false
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    this.setData({
      taskId: options.taskId || '',
      businessType: options.businessType || '',
      businessId: options.businessId ? parseInt(options.businessId) : null,
      businessTitle: decodeURIComponent(options.businessTitle || ''),
      businessSubtitle: decodeURIComponent(options.businessSubtitle || ''),
      nodeName: decodeURIComponent(options.nodeName || ''),
      createTime: options.createTime || '',
      canComplete: auth.hasPermission('task:complete')
    })

    this.loadHistory()
  },

  /** 加载审批历史 */
  async loadHistory() {
    const { businessType, businessId } = this.data
    if (!businessType || !businessId) return

    this.setData({ historyLoading: true })
    try {
      const records = await approvalApi.history(businessType, businessId)
      this.setData({ history: records || [] })
    } catch {
      // 历史加载失败不影响审批操作
    } finally {
      this.setData({ historyLoading: false })
    }
  },

  /** 审批意见输入 */
  onCommentInput(e) {
    this.setData({ comment: e.detail.value })
  },

  /** 审批通过 */
  async handleApprove() {
    if (this.data.submitting) return
    this.setData({ submitting: true })

    try {
      await approvalApi.complete(this.data.taskId, true, this.data.comment)
      wx.showToast({ title: '审批通过', icon: 'success' })
      this.notifyRefreshAndBack()
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  /** 审批驳回 */
  async handleReject() {
    if (this.data.submitting) return
    if (!this.data.comment.trim()) {
      wx.showToast({ title: '驳回须填写审批意见', icon: 'none' })
      return
    }
    this.setData({ submitting: true })

    try {
      await approvalApi.complete(this.data.taskId, false, this.data.comment)
      wx.showToast({ title: '已驳回', icon: 'success' })
      this.notifyRefreshAndBack()
    } catch (e) {
      wx.showToast({ title: e.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  /** 通知列表页刷新并返回 */
  notifyRefreshAndBack() {
    const pages = getCurrentPages()
    const prevPage = pages[pages.length - 2]
    if (prevPage) {
      prevPage.setData({ needRefresh: true })
    }
    setTimeout(() => { wx.navigateBack() }, 1000)
  }
})
