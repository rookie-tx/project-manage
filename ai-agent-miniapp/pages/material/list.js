const auth = require('../../utils/auth')
const materialApi = require('../../api/material')
const dictCache = require('../../utils/dictCache')

Page({
  data: {
    items: [],
    loading: false,
    canCreate: false,
    canDelete: false,
    unitLabelMap: {}
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({
      canCreate: auth.hasPermission('material:create'),
      canDelete: auth.hasPermission('material:delete')
    })
    this.loadUnits().then(() => { this.loadItems() })
  },

  onShow() {
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadItems()
    }
  },

  async loadUnits() {
    try {
      const items = await dictCache.getDict('UNIT')
      const map = {}
      items.forEach(item => { map[item.code] = item.label })
      this.setData({ unitLabelMap: map })
    } catch (e) { /* ignore */ }
  },

  async loadItems() {
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const items = await materialApi.list()
      const map = this.data.unitLabelMap
      const enriched = (items || []).map(item => ({
        ...item,
        unitLabel: map[item.unit] || item.unit || ''
      }))
      this.setData({ items: enriched })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  getUnitLabel(code) {
    return this.data.unitLabelMap[code] || code || ''
  },

  handleCreate() {
    wx.navigateTo({ url: '/pages/material/form' })
  },

  handleEdit(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/material/form?id=' + id })
  },

  handleDelete(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该物料吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await materialApi.del(id)
            wx.showToast({ title: '删除成功', icon: 'success' })
            this.loadItems()
          } catch (e) {
            wx.showToast({ title: e.message || '删除失败', icon: 'none' })
          }
        }
      }
    })
  }
})
