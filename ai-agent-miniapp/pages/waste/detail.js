const auth = require('../../utils/auth')
const wasteApi = require('../../api/constructionWaste')

const API_BASE = 'https://legwork-blush-vitally.ngrok-free.dev'

Page({
  data: {
    id: null,
    record: null,
    loading: true,
    canDelete: false,
    // 格式化后的字段
    removalDateStr: '',
    quantityStr: '',
    createdAtStr: '',
    // 照片
    photoUrls: [],
    photoNames: []
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const id = options.id ? parseInt(options.id) : null
    if (!id) {
      this.setData({ loading: false })
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    this.setData({ id: id, canDelete: auth.hasPermission('waste:delete') })
    this.loadDetail()
  },

  async loadDetail() {
    this.setData({ loading: true })
    try {
      const record = await wasteApi.getById(this.data.id)
      // 构造完整照片 URL
      const photoUrls = (record.files || []).map(f => API_BASE + f.url)
      const photoNames = (record.files || []).map(f => f.originalName || '照片')

      this.setData({
        record: record,
        removalDateStr: formatDate(record.removalDate),
        quantityStr: record.quantity + ' ' + (record.unit || ''),
        createdAtStr: formatDateTime(record.createdAt),
        photoUrls: photoUrls,
        photoNames: photoNames
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 返回列表 */
  navigateBack() {
    wx.navigateBack()
  },

  /** 预览照片 */
  handlePreviewPhoto(e) {
    const idx = e.currentTarget.dataset.index
    wx.previewImage({
      current: this.data.photoUrls[idx],
      urls: this.data.photoUrls
    })
  },

  /** 删除记录 */
  handleDelete() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该运出记录吗？关联的照片将一并删除。',
      confirmText: '确认删除',
      confirmColor: '#ff4d4f',
      cancelText: '取消',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await wasteApi.del(this.data.id)
          wx.showToast({ title: '已删除', icon: 'success' })
          // 通知列表页刷新
          const pages = getCurrentPages()
          const prevPage = pages[pages.length - 2]
          if (prevPage) {
            prevPage.setData({ needRefresh: true })
          }
          setTimeout(() => { wx.navigateBack() }, 1000)
        } catch (e) {
          wx.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    })
  }
})

/** 从日期字符串提取日期 YYYY-MM-DD */
function formatDate(dateStr) {
  if (!dateStr) return ''
  if (typeof dateStr === 'string' && dateStr.length >= 10) return dateStr.substring(0, 10)
  return dateStr
}

/** 解析 ISO 日期时间为可读格式 */
function formatDateTime(dt) {
  if (!dt) return ''
  if (typeof dt === 'string') {
    return dt.substring(0, 16).replace('T', ' ')
  }
  return dt
}
