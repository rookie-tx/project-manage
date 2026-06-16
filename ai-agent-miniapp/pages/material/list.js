const auth = require('../../utils/auth')
const materialApi = require('../../api/material')

const UNITS = [
  { code: 0, label: '个' }, { code: 1, label: '台' },
  { code: 2, label: '套' }, { code: 3, label: '米' },
  { code: 4, label: '吨' }, { code: 5, label: '立方米' },
  { code: 6, label: '卷' }, { code: 7, label: '桶' },
  { code: 8, label: '箱' }, { code: 9, label: '其他' }
]

Page({
  data: {
    items: [],
    loading: false,
    canCreate: false,
    canDelete: false
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
    this.loadItems()
  },

  onShow() {
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadItems()
    }
  },

  async loadItems() {
    if (this.data.loading) return
    this.setData({ loading: true })
    try {
      const items = await materialApi.list()
      const enriched = (items || []).map(item => ({
        ...item,
        unitLabel: this.getUnitLabel(item.unit)
      }))
      this.setData({ items: enriched })
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  getUnitLabel(code) {
    const u = UNITS.find(u => u.code === code)
    return u ? u.label : '未知'
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
