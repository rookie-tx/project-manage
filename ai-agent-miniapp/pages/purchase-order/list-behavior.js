const auth = require('../../utils/auth')
const poApi = require('../../api/purchaseOrder')

const PAGE_SIZE = 20
const STATUS_LABEL = { 0: '待采购', 1: '已采购', 2: '已到货' }

/**
 * 采购单列表共享行为
 * @param {number|null} statusFilter - null=汇总, 0=待采购, 1=已采购, 2=已到货
 * @returns {object} Page 定义对象（data + 生命周期 + 方法）
 */
function createListBehavior(statusFilter) {
  return {
    data: {
      items: [],
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
      this.loadItems(true)
    },

    onShow() {
      if (this.data.needRefresh) {
        this.setData({ needRefresh: false })
        this.loadItems(true)
      }
    },

    onPullDownRefresh() {
      this.setData({ refreshing: true })
      this.loadItems(true)
      wx.stopPullDownRefresh()
    },

    onReachBottom() {
      if (!this.data.hasMore || this.data.loading) return
      this.loadItems(false)
    },

    async loadItems(reset) {
      if (this.data.loading) return
      const page = reset ? 1 : this.data.page
      this.setData({ loading: true })
      try {
        const params = { page, size: PAGE_SIZE }
        if (statusFilter === 0) {
          params.tab = 'pending'
        } else if (statusFilter !== null) {
          params.status = statusFilter
        }
        const result = await poApi.list(params)
        const items = (result.items || []).map(r => {
          const pr = (r.purchaseRequests && r.purchaseRequests[0]) || {}
          return {
            ...r,
            purchaseDateStr: formatDate(r.purchaseDate),
            statusLabel: STATUS_LABEL[r.statusCode] || '未知',
            prName: pr.name || '-',
            prCode: pr.code || '',
            prSpec: pr.specification || '',
            prMaterialName: pr.materialName || '',
            prMaterialSpec: pr.materialSpec || '',
            prQtyStr: pr.quantityStr || '',
            prProjectName: pr.projectName || ''
          }
        })
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
        wx.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.setData({ loading: false, refreshing: false })
      }
    },

    // 点击卡片 → 详情
    handleTap(e) {
      const id = e.currentTarget.dataset.id
      wx.navigateTo({ url: '/pages/purchase-order/detail?id=' + id })
    },

    // 待采购 → 完善信息
    handleEdit(e) {
      const id = e.currentTarget.dataset.id
      wx.navigateTo({ url: '/pages/purchase-order/form?id=' + id })
    },

    // 已采购 → 入库
    handleInbound(e) {
      const id = e.currentTarget.dataset.id
      const prId = e.currentTarget.dataset.prid
      wx.navigateTo({ url: '/pages/purchase-order/inbound?id=' + id + '&prId=' + (prId || '') })
    }
  }
}

function formatDate(d) {
  if (!d) return ''
  if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10)
  return d
}

module.exports = { createListBehavior }
