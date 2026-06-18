/**
 * 待报价物料列表页
 */
const quotationApi = require('../../api/quotation')
const auth = require('../../utils/auth')

const PAGE_SIZE = 20

Page({
  data: {
    items: [],
    loading: false,
    hasMore: true,
    page: 1,
    refreshing: false
  },

  onLoad() {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.loadItems(true)
  },

  onShow() {
    if (this.data.page > 1) {
      this.loadItems(true)
    }
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadItems(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadItems(false)
    }
  },

  async loadItems(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })

    try {
      const result = await quotationApi.listPendingOrders({ page, size: PAGE_SIZE })
      const items = (result.records || []).map(r => ({
        id: r.id,
        materialName: r.materialName || '-',
        specification: r.specification || '-',
        brand: r.brand || '-',
        quantityStr: r.quantityStr || '-',
        projectName: r.projectName || '-',
        expectedArrivalDate: r.expectedArrivalDate || '-',
        statusCode: r.statusCode,
        statusLabel: '采购中'
      }))

      if (reset) {
        this.setData({ items, page: page + 1, hasMore: items.length >= PAGE_SIZE })
      } else {
        this.setData({
          items: this.data.items.concat(items),
          page: page + 1,
          hasMore: items.length >= PAGE_SIZE
        })
      }
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
      wx.stopPullDownRefresh()
    }
  },

  /** 进入报价 */
  handleQuote(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/quotation/submit?id=' + id })
  }
})
