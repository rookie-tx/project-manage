const auth = require('../../utils/auth')
const purchaseApi = require('../../api/purchase')
const approvalApi = require('../../api/approval')
const dictCache = require('../../utils/dictCache')

const STATUS_MAP = { 0: '待提交', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已取消' }

Page({
  data: {
    id: null,
    record: null,
    loading: true,
    isPending: false,

    // 审批历史
    history: [],
    historyLoading: false,

    // 提交审批
    canSubmit: false,
    users: [],
    userNames: [],
    pmUserIdx: -1,
    submittingApproval: false,

    // 格式化字段
    quantityStr: '',
    arrivalDateStr: '',
    statusLabel: '',
    createdAtStr: '',

    // 单位标签映射
    unitLabelMap: {}
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({
      id: options.id ? parseInt(options.id) : null,
      canSubmit: auth.hasPermission('pr:approve')
    })
    if (options.showSubmit === '1') {
      // 从列表"提交审批"按钮跳转，自动滚动到审批区
      this.setData({ focusSubmit: true })
    }
    this.loadUnitLabels().then(() => this.loadDetail())
  },

  async loadUnitLabels() {
    try {
      const items = await dictCache.getDict('UNIT')
      const map = {}
      items.forEach(item => { map[item.code] = item.label })
      this.setData({ unitLabelMap: map })
    } catch (e) { /* ignore */ }
  },

  getUnitLabel(code) {
    return this.data.unitLabelMap[code] || code || ''
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const record = await purchaseApi.detail(this.data.id)
      this.setData({
        record: record,
        isPending: record.statusCode === 0,
        quantityStr: (record.quantity || '') + ' ' + this.getUnitLabel(record.unitCode),
        arrivalDateStr: formatDate(record.arrivalDate),
        statusLabel: STATUS_MAP[record.statusCode] || '未知',
        createdAtStr: formatDateTime(record.createdAt)
      })
      this.loadHistory()
      if (record.statusCode === 0) this.loadUsers()
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  async loadHistory() {
    this.setData({ historyLoading: true })
    try {
      const records = await approvalApi.history('PURCHASE_REQUEST', this.data.id)
      this.setData({ history: records || [] })
    } catch (e) { /* ignore */ }
    finally { this.setData({ historyLoading: false }) }
  },

  async loadUsers() {
    try {
      const users = await purchaseApi.getUsers()
      this.setData({
        users: users || [],
        userNames: (users || []).map(u => u.name || u.username)
      })
    } catch (e) { /* ignore */ }
  },

  onPmUserChange(e) { this.setData({ pmUserIdx: parseInt(e.detail.value) }) },

  async handleSubmitApproval() {
    const { users, pmUserIdx } = this.data
    if (pmUserIdx < 0) {
      wx.showToast({ title: '请选择项目经理', icon: 'none' })
      return
    }

    this.setData({ submittingApproval: true })
    try {
      const client = require('../../api/client')
      await client.post('/tasks/start', {
        businessType: 'PURCHASE_REQUEST',
        businessId: this.data.id,
        variables: {
          pmUserId: users[pmUserIdx].id
        }
      })
      wx.showToast({ title: '提交审批成功', icon: 'success' })
      this.loadDetail()
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submittingApproval: false })
    }
  },

  handleEdit() {
    wx.navigateTo({ url: '/pages/purchase/form?id=' + this.data.id })
  },

  handleDelete() {
    wx.showModal({
      title: '确认删除',
      content: '删除后数据不可恢复，确定要删除此采购申请吗？',
      confirmText: '确认删除',
      confirmColor: '#ff4d4f',
      cancelText: '取消',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await purchaseApi.del(this.data.id)
          wx.showToast({ title: '已删除', icon: 'success' })
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage) prevPage.setData({ needRefresh: true })
          setTimeout(() => wx.navigateBack(), 1000)
        } catch (e) {
          wx.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    })
  }
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
function formatDateTime(dt) { if (!dt) return ''; if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' '); return dt }
