const auth = require('../../utils/auth')
const purchaseApi = require('../../api/purchase')
const approvalApi = require('../../api/approval')

const UNIT_MAP = { 0: '个', 1: '台', 2: '套', 3: '米', 4: '吨', 5: '立方米', 6: '卷', 7: '桶', 8: '箱', 9: '其他' }
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
    deptMgrIdx: -1,
    financeMgrIdx: -1,
    generalMgrIdx: -1,
    submittingApproval: false,

    // 格式化字段
    quantityStr: '',
    arrivalDateStr: '',
    statusLabel: '',
    createdAtStr: ''
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
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const record = await purchaseApi.detail(this.data.id)
      this.setData({
        record: record,
        isPending: record.statusCode === 0,
        quantityStr: (record.quantity || '') + ' ' + (UNIT_MAP[record.unitCode] || ''),
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
    } catch { /* ignore */ }
    finally { this.setData({ historyLoading: false }) }
  },

  async loadUsers() {
    try {
      const users = await purchaseApi.getUsers()
      this.setData({
        users: users || [],
        userNames: (users || []).map(u => u.username)
      })
    } catch { /* ignore */ }
  },

  onDeptMgrChange(e) { this.setData({ deptMgrIdx: parseInt(e.detail.value) }) },
  onFinanceMgrChange(e) { this.setData({ financeMgrIdx: parseInt(e.detail.value) }) },
  onGeneralMgrChange(e) { this.setData({ generalMgrIdx: parseInt(e.detail.value) }) },

  async handleSubmitApproval() {
    const { users, deptMgrIdx, financeMgrIdx, generalMgrIdx } = this.data
    const selectedIds = []
    if (deptMgrIdx >= 0) selectedIds.push(users[deptMgrIdx].id)
    if (financeMgrIdx >= 0) selectedIds.push(users[financeMgrIdx].id)
    if (generalMgrIdx >= 0) selectedIds.push(users[generalMgrIdx].id)

    if (deptMgrIdx < 0 || financeMgrIdx < 0 || generalMgrIdx < 0) {
      wx.showToast({ title: '三个审批人均为必选', icon: 'none' })
      return
    }

    this.setData({ submittingApproval: true })
    try {
      const client = require('../../api/client')
      await client.post('/tasks/start', {
        businessType: 'PURCHASE_REQUEST',
        businessId: this.data.id,
        variables: {
          deptManagerId: deptMgrIdx >= 0 ? users[deptMgrIdx].id : null,
          financeManagerId: financeMgrIdx >= 0 ? users[financeMgrIdx].id : null,
          generalManagerId: generalMgrIdx >= 0 ? users[generalMgrIdx].id : null
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
