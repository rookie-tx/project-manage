const auth = require('../../utils/auth')
const wasteApi = require('../../api/constructionWaste')

const PAGE_SIZE = 20

Page({
  data: {
    // 权限
    canCreate: false,

    // 筛选
    projects: [],            // [{ id, name }]
    projectNames: ['全部项目'],
    selectedProjectIdx: 0,
    wasteType: '',
    startDate: '',
    endDate: '',

    // 列表
    records: [],
    loading: false,
    hasMore: true,
    offset: 0,
    refreshing: false,
    needRefresh: false
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ canCreate: auth.hasPermission('waste:create') })
    this.loadProjects()
    this.loadRecords(true)
  },

  onShow() {
    // 从创建页或详情页返回时，如果标记了需要刷新则重新加载
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadRecords(true)
    }
  },

  /** 加载项目列表（供筛选 picker 使用） */
  async loadProjects() {
    try {
      const projects = await wasteApi.getProjects()
      this.setData({
        projects: projects || [],
        projectNames: ['全部项目'].concat((projects || []).map(p => p.name))
      })
    } catch {
      // 项目列表加载失败不影响列表查询
    }
  },

  /** 加载废渣记录列表 */
  async loadRecords(reset) {
    if (this.data.loading) return
    const offset = reset ? 0 : this.data.offset
    this.setData({ loading: true })

    try {
      const params = { offset: offset, limit: PAGE_SIZE }
      const { selectedProjectIdx, projects, wasteType, startDate, endDate } = this.data
      if (selectedProjectIdx > 0 && projects[selectedProjectIdx - 1]) {
        params.projectId = projects[selectedProjectIdx - 1].id
      }
      if (wasteType.trim()) params.wasteType = wasteType.trim()
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate

      const records = await wasteApi.list(params)
      const formatted = (records || []).map(r => ({
        ...r,
        removalDateStr: formatDate(r.removalDate),
        quantityStr: r.quantity + ' ' + (r.unit || '')
      }))

      if (reset) {
        this.setData({ records: formatted, offset: formatted.length, hasMore: formatted.length >= PAGE_SIZE })
      } else {
        this.setData({
          records: this.data.records.concat(formatted),
          offset: offset + formatted.length,
          hasMore: formatted.length >= PAGE_SIZE
        })
      }
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  /** 下拉刷新 */
  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadRecords(true)
    wx.stopPullDownRefresh()
  },

  /** 上拉加载更多 */
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadRecords(false)
  },

  /** 项目筛选变更 */
  onProjectChange(e) {
    this.setData({ selectedProjectIdx: parseInt(e.detail.value) })
    this.loadRecords(true)
  },

  /** 废渣种类输入 */
  onWasteTypeInput(e) {
    this.setData({ wasteType: e.detail.value })
  },

  /** 种类搜索（失焦或确认时触发） */
  onWasteTypeSearch() {
    this.loadRecords(true)
  },

  /** 日期筛选变更 */
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value })
    this.loadRecords(true)
  },

  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value })
    this.loadRecords(true)
  },

  /** 清除全部筛选 */
  clearFilters() {
    this.setData({
      selectedProjectIdx: 0,
      wasteType: '',
      startDate: '',
      endDate: ''
    })
    this.loadRecords(true)
  },

  /** 跳转详情 */
  handleRecordTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/waste/detail?id=' + id })
  },

  /** 跳转新建 */
  handleCreate() {
    wx.navigateTo({ url: '/pages/waste/create' })
  }
})

/** 从日期字符串提取日期 YYYY-MM-DD */
function formatDate(dateStr) {
  if (!dateStr) return ''
  if (typeof dateStr === 'string' && dateStr.length >= 10) return dateStr.substring(0, 10)
  return dateStr
}
