const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')

const STATUSES = { 0: '待采购', 1: '已采购', 2: '已到货' }

Page({
  data: {
    id: null, record: null, loading: true, needRefresh: false,
    statusLabel: '', purchaseDateStr: '', createdAtStr: '',
    photoUrls: [], prInfo: null
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({ id: options.id ? parseInt(options.id) : null })
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
        record: r,
        statusLabel: STATUSES[r.statusCode] || '未知',
        purchaseDateStr: formatDate(r.purchaseDate),
        createdAtStr: formatDateTime(r.createdAt),
        photoUrls: (r.files || []).map(f => API_BASE + f.url),
        prInfo: (r.purchaseRequests && r.purchaseRequests[0]) || null
      })
    } catch (e) { wx.showToast({ title: e.message || '加载失败', icon: 'none' }) }
    finally { this.setData({ loading: false }) }
  },

  handlePreviewPhoto(e) {
    wx.previewImage({ current: this.data.photoUrls[e.currentTarget.dataset.index], urls: this.data.photoUrls })
  },
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
function formatDateTime(dt) { if (!dt) return ''; if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' '); return dt }
