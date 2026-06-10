const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')

const STATUSES = { 0: '采购中', 1: '已采购', 2: '已到货' }

Page({
  data: {
    id: null, record: null, loading: true, needRefresh: false,
    canEdit: false, canDelete: false,
    statusLabel: '', purchaseDateStr: '', createdAtStr: '',
    photoUrls: []
  },
  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({ id: options.id ? parseInt(options.id) : null, canEdit: auth.hasPermission('purchase:update'), canDelete: auth.hasPermission('purchase:delete') })
    this.loadDetail()
  },
  onShow() {
    if (this.data.needRefresh) { this.setData({ needRefresh: false }); this.loadDetail() }
  },
  async loadDetail() {
    this.setData({ loading: true })
    try {
      const r = await poApi.detail(this.data.id)
      const API_BASE = 'https://legwork-blush-vitally.ngrok-free.dev'
      this.setData({
        record: r, statusLabel: STATUSES[r.statusCode] || '未知',
        purchaseDateStr: formatDate(r.purchaseDate), createdAtStr: formatDateTime(r.createdAt),
        photoUrls: (r.files || []).map(f => API_BASE + f.url),
        purchaseRequests: r.purchaseRequests || []
      })
    } catch (e) { wx.showToast({ title: e.message || '加载失败', icon: 'none' }) }
    finally { this.setData({ loading: false }) }
  },
  handlePreviewPhoto(e) { wx.previewImage({ current: this.data.photoUrls[e.currentTarget.dataset.index], urls: this.data.photoUrls }) },
  handleEdit() { wx.navigateTo({ url: '/pages/purchase-order/form?id=' + this.data.id }) },
  handleDelete() {
    wx.showModal({ title: '确认删除', content: '确定要删除此采购记录吗？', confirmText: '确认删除', confirmColor: '#ef4444', cancelText: '取消', success: async (res) => {
      if (!res.confirm) return
      try { await poApi.del(this.data.id); wx.showToast({ title: '已删除', icon: 'success' })
        const pages = getCurrentPages(); const prev = pages[pages.length - 2]; if (prev) prev.setData({ needRefresh: true })
        setTimeout(() => wx.navigateBack(), 1000) }
      catch (e) { wx.showToast({ title: e.message || '删除失败', icon: 'none' }) }
    }})
  },
  async handleChangeStatus() {
    const next = 1 + (this.data.record.statusCode || 0)
    if (next > 2) return
    const labels = ['已采购', '已到货']
    wx.showModal({ title: '状态变更', content: '确认将状态变更为「' + labels[next-1] + '」吗？', confirmText: '确认', cancelText: '取消', success: async (res) => {
      if (!res.confirm) return
      try { await poApi.changeStatus(this.data.id, next); wx.showToast({ title: '状态已更新', icon: 'success' }); this.loadDetail() }
      catch (e) { wx.showToast({ title: e.message || '操作失败', icon: 'none' }) }
    }})
  }
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
function formatDateTime(dt) { if (!dt) return ''; if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' '); return dt }
