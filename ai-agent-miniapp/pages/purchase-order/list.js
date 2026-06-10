const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')

const PAGE_SIZE = 20
const STATUSES = ['全部状态', '采购中', '已采购', '已到货']

Page({
  data: {
    canCreate: false, statusIdx: 0,
    items: [], loading: false, hasMore: true, page: 1, refreshing: false, needRefresh: false
  },
  onLoad() {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({ canCreate: auth.hasPermission('purchase:create') })
    this.loadItems(true)
  },
  onShow() { if (this.data.needRefresh) { this.setData({ needRefresh: false }); this.loadItems(true) } },
  async loadItems(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page; this.setData({ loading: true })
    try {
      const params = { page, size: PAGE_SIZE }
      if (this.data.statusIdx > 0) params.status = this.data.statusIdx - 1
      const result = await poApi.list(params)
      const items = (result.items || []).map(r => ({
        ...r, purchaseDateStr: formatDate(r.purchaseDate), statusLabel: STATUSES[r.statusCode + 1] || '未知',
        prNames: (r.purchaseRequests || []).map(p => p.code || p.name).join('、') || null,
        prDetails: (r.purchaseRequests || []).map(p => ({ id: p.id, code: p.code || '', name: p.name, quantityStr: p.quantityStr || '' }))
      }))
      if (reset) { this.setData({ items, page: page + 1, hasMore: items.length >= PAGE_SIZE }) }
      else { this.setData({ items: this.data.items.concat(items), page: page + 1, hasMore: items.length >= PAGE_SIZE }) }
    } catch { wx.showToast({ title: '加载失败', icon: 'none' }) }
    finally { this.setData({ loading: false, refreshing: false }) }
  },
  onPullDownRefresh() { this.setData({ refreshing: true }); this.loadItems(true); wx.stopPullDownRefresh() },
  onReachBottom() { if (!this.data.hasMore || this.data.loading) return; this.loadItems(false) },
  onStatusChange(e) { this.setData({ statusIdx: parseInt(e.detail.value) }); this.loadItems(true) },
  handleTap(e) { wx.navigateTo({ url: '/pages/purchase-order/detail?id=' + e.currentTarget.dataset.id }) },
  handleCreate() { wx.navigateTo({ url: '/pages/purchase-order/form' }) }
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
