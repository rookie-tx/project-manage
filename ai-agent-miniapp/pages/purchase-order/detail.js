const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')

const STATUSES = { 0: '待采购', 1: '已采购', 2: '已到货' }

Page({
  data: {
    id: null, record: null, loading: true, needRefresh: false,
    statusLabel: '', purchaseDateStr: '', createdAtStr: '',
    photoUrls: [], prInfo: null,
    quotations: [], hasSelected: false
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({ id: options.id ? parseInt(options.id) : null })
    this.loadDetail()
    this.loadQuotations()
  },

  onShow() {
    if (this.data.needRefresh) { this.setData({ needRefresh: false }); this.loadDetail(); this.loadQuotations() }
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const r = await poApi.detail(this.data.id)
      const API_BASE = 'https://legwork-blush-vitally.ngrok-free.dev'
      this.setData({
        record: {
          ...r,
          purchaserName: r.purchaserName || '',
          inboundOperatorName: r.inboundOperatorName || '',
          createdByName: r.createdByName || ''
        },
        statusLabel: STATUSES[r.statusCode] || '未知',
        purchaseDateStr: formatDate(r.purchaseDate),
        createdAtStr: formatDateTime(r.createdAt),
        photoUrls: (r.files || []).map(f => API_BASE + f.url),
        prInfo: (r.purchaseRequests && r.purchaseRequests[0]) || null
      })
    } catch (e) { wx.showToast({ title: e.message || '加载失败', icon: 'none' }) }
    finally { this.setData({ loading: false }) }
  },

  async loadQuotations() {
    try {
      const result = await poApi.fetchQuotations(this.data.id)
      const quotations = (result.records || []).map(q => ({
        id: q.id,
        companyName: q.supplierCompanyName || '-',
        contactPerson: q.supplierContactPerson || '-',
        contactPhone: q.supplierContactPhone || '-',
        unitPrice: q.unitPrice,
        totalAmount: q.totalAmount,
        unitPriceStr: q.unitPrice != null ? '¥' + parseFloat(q.unitPrice).toFixed(2) : '-',
        totalAmountStr: q.totalAmount != null ? '¥' + parseFloat(q.totalAmount).toFixed(2) : '-',
        deliveryDate: q.deliveryDate || '-',
        remark: q.remark || '-',
        statusCode: q.statusCode,
        statusLabel: q.statusLabel
      }))
      const hasSelected = quotations.some(q => q.statusCode === 1)
      this.setData({ quotations, hasSelected })
    } catch (e) {
      console.error('加载报价失败', e)
      this.setData({ quotations: [], hasSelected: false })
    }
  },

  handleSelectQuotation(e) {
    const idx = e.currentTarget.dataset.index
    const q = this.data.quotations[idx]
    if (!q) return
    wx.showModal({
      title: '确认选用报价',
      content: `选用「${q.companyName}」的报价后，采购单将自动流转为「已采购」状态。\n\n单价：¥${(q.unitPrice || 0).toFixed(2)}\n总金额：¥${(q.totalAmount || 0).toFixed(2)}`,
      confirmText: '确认选用',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await poApi.selectQuotation(this.data.id, q.id)
          wx.showToast({ title: '采购完成', icon: 'success' })
          // 通知上一页刷新
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage) { prevPage.setData({ needRefresh: true }) }
          setTimeout(() => wx.navigateBack(), 1500)
        } catch (e) {
          wx.showToast({ title: e.message || '选用失败', icon: 'none' })
        }
      }
    })
  },

  handlePreviewPhoto(e) {
    wx.previewImage({ current: this.data.photoUrls[e.currentTarget.dataset.index], urls: this.data.photoUrls })
  },
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
function formatDateTime(dt) { if (!dt) return ''; if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' '); return dt }
