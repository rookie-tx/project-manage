const auth = require('../../utils/auth')
const projectApi = require('../../api/project')

const TYPES = { 0: '房建', 1: '市政', 2: '装修', 3: '路桥', 4: '水利', 9: '其他' }
const STATUSES = { 0: '筹备中', 1: '在建', 2: '停工', 3: '已竣工', 4: '已结算' }

Page({
  data: {
    id: null, record: null, loading: true,
    canEdit: false, canDelete: false,
    typeLabel: '', statusLabel: '',
    plannedStr: '', createdAtStr: ''
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({
      id: options.id ? parseInt(options.id) : null,
      canEdit: auth.hasPermission('project:update'),
      canDelete: auth.hasPermission('project:delete')
    })
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const r = await projectApi.detail(this.data.id)
      this.setData({
        record: r,
        typeLabel: TYPES[r.type] || '未知',
        statusLabel: STATUSES[r.status] || '未知',
        plannedStr: formatDate(r.plannedStartDate) + ' ~ ' + formatDate(r.plannedEndDate),
        createdAtStr: formatDateTime(r.createdAt)
      })
    } catch (e) { wx.showToast({ title: e.message || '加载失败', icon: 'none' }) }
    finally { this.setData({ loading: false }) }
  },

  handleEdit() { wx.navigateTo({ url: '/pages/project/form?id=' + this.data.id }) },

  handleDelete() {
    wx.showModal({
      title: '确认删除', content: '删除后数据不可恢复，确定要删除此项目吗？',
      confirmText: '确认删除', confirmColor: '#ef4444', cancelText: '取消',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await projectApi.del(this.data.id)
          wx.showToast({ title: '已删除', icon: 'success' })
          const pages = getCurrentPages(); const prev = pages[pages.length - 2]
          if (prev) prev.setData({ needRefresh: true })
          setTimeout(() => wx.navigateBack(), 1000)
        } catch (e) { wx.showToast({ title: e.message || '删除失败', icon: 'none' }) }
      }
    })
  }
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
function formatDateTime(dt) { if (!dt) return ''; if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' '); return dt }
