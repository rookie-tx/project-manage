const auth = require('../../utils/auth')
const projectApi = require('../../api/project')

const PAGE_SIZE = 20

const TYPES = ['全部类型', '房建', '市政', '装修', '路桥', '水利', '其他']
const TYPE_VALUES = [null, 0, 1, 2, 3, 4, 9]
const STATUSES = ['全部状态', '筹备中', '在建', '停工', '已竣工', '已结算']
const STATUS_VALUES = [null, 0, 1, 2, 3, 4]

Page({
  data: {
    canCreate: false,
    keyword: '',
    typeIdx: 0,
    statusIdx: 0,
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
    this.setData({ canCreate: auth.hasPermission('project:create') })
    this.loadItems(true)
  },

  onShow() {
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadItems(true)
    }
  },

  async loadItems(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })

    try {
      const params = { page: page, size: PAGE_SIZE }
      const { keyword, typeIdx, statusIdx } = this.data
      if (keyword.trim()) params.keyword = keyword.trim()
      if (typeIdx > 0) params.type = TYPE_VALUES[typeIdx]
      if (statusIdx > 0) params.status = STATUS_VALUES[statusIdx]

      const result = await projectApi.list(params)
      const items = (result.items || []).map(r => ({
        ...r,
        typeLabel: TYPES[r.type + 1] || '未知',
        statusLabel: STATUSES[r.status + 1] || '未知',
        plannedStr: formatDate(r.plannedStartDate) + ' ~ ' + formatDate(r.plannedEndDate)
      }))

      if (reset) {
        this.setData({ items, page: page + 1, hasMore: items.length >= PAGE_SIZE })
      } else {
        this.setData({ items: this.data.items.concat(items), page: page + 1, hasMore: items.length >= PAGE_SIZE })
      }
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  onPullDownRefresh() { this.setData({ refreshing: true }); this.loadItems(true); wx.stopPullDownRefresh() },
  onReachBottom() { if (!this.data.hasMore || this.data.loading) return; this.loadItems(false) },

  onKeywordInput(e) { this.setData({ keyword: e.detail.value }) },
  handleSearch() { this.loadItems(true) },
  onTypeChange(e) { this.setData({ typeIdx: parseInt(e.detail.value) }); this.loadItems(true) },
  onStatusChange(e) { this.setData({ statusIdx: parseInt(e.detail.value) }); this.loadItems(true) },

  handleTap(e) { wx.navigateTo({ url: '/pages/project/detail?id=' + e.currentTarget.dataset.id }) },
  handleCreate() { wx.navigateTo({ url: '/pages/project/form' }) }
})

function formatDate(d) { if (!d) return ''; if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10); return d }
