const auth = require('../../utils/auth')
const location = require('../../utils/location')
const attendanceApi = require('../../api/attendance')

Page({
  data: {
    username: '',
    // 今日打卡状态
    todayRecord: null,
    isClockedIn: false,
    isClockedOut: false,
    clockInTime: '',
    clockOutTime: '',
    // 操作状态
    loading: false,
    locating: false,
    // 本月记录
    records: [],
    recordsLoading: false,
    currentMonth: ''
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = auth.getUser()
    this.setData({ username: user ? user.username : '未知' })
    const now = new Date()
    const m = now.getMonth() + 1
    this.setData({ currentMonth: now.getFullYear() + '-' + (m < 10 ? '0' + m : m) })
    this.fetchToday()
    this.fetchRecords()
  },

  /** 获取今日打卡状态 */
  async fetchToday() {
    try {
      const records = await attendanceApi.getMyRecords(this.data.currentMonth)
      if (records && records.length > 0) {
        const today = new Date().toISOString().substring(0, 10)
        const todayRecord = records.find(r => r.clockIn && r.clockIn.startsWith(today))
        if (todayRecord) {
          this.setData({
            todayRecord: todayRecord,
            isClockedIn: true,
            isClockedOut: todayRecord.status === 'CLOCKED_OUT',
            clockInTime: formatTime(todayRecord.clockIn),
            clockOutTime: todayRecord.clockOut ? formatTime(todayRecord.clockOut) : ''
          })
          return
        }
      }
      this.setData({ todayRecord: null, isClockedIn: false, isClockedOut: false, clockInTime: '', clockOutTime: '' })
    } catch {
      this.setData({ todayRecord: null })
    }
  },

  /** 获取本月打卡记录 */
  async fetchRecords() {
    this.setData({ recordsLoading: true })
    try {
      const records = await attendanceApi.getMyRecords(this.data.currentMonth)
      // 在 JS 层预处理时间格式，WXML 不支持直接调用 substring
      const formatted = (records || []).map(r => ({
        id: r.id,
        dateStr: formatDate(r.clockIn),
        clockInStr: formatTime(r.clockIn),
        clockOutStr: formatTime(r.clockOut),
        isDone: r.status === 'CLOCKED_OUT'
      }))
      this.setData({ records: formatted })
    } catch {
      this.setData({ records: [] })
    } finally {
      this.setData({ recordsLoading: false })
    }
  },

  /** 上班打卡 */
  async handleClockIn() {
    if (this.data.locating || this.data.loading) return
    this.setData({ locating: true })
    try {
      const loc = await location.getCurrentLocation()
      this.setData({ locating: false, loading: true })
      await attendanceApi.clockIn(loc)
      wx.showToast({ title: '上班打卡成功', icon: 'success' })
      this.fetchToday()
      this.fetchRecords()
    } catch (e) {
      wx.showToast({ title: e.message || '打卡失败', icon: 'none' })
      this.setData({ locating: false })
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 下班打卡 */
  async handleClockOut() {
    if (this.data.locating || this.data.loading) return
    this.setData({ locating: true })
    try {
      const loc = await location.getCurrentLocation()
      this.setData({ locating: false, loading: true })
      await attendanceApi.clockOut(loc)
      wx.showToast({ title: '下班打卡成功', icon: 'success' })
      this.fetchToday()
      this.fetchRecords()
    } catch (e) {
      wx.showToast({ title: e.message || '打卡失败', icon: 'none' })
      this.setData({ locating: false })
    } finally {
      this.setData({ loading: false })
    }
  }
})

/** 从日期字符串提取时间 HH:mm */
function formatTime(dateStr) {
  if (!dateStr) return '--:--'
  // 兼容 ISO 格式 "2026-06-03T08:30:00" 和非标准格式
  const tIdx = dateStr.indexOf('T')
  if (tIdx >= 0) return dateStr.substring(tIdx + 1, tIdx + 6)
  // 兼容空格分隔格式 "2026-06-03 08:30:00"
  if (dateStr.length >= 16) return dateStr.substring(11, 16)
  return '--:--'
}

/** 从日期字符串提取日期 YYYY-MM-DD */
function formatDate(dateStr) {
  if (!dateStr) return ''
  if (dateStr.length >= 10) return dateStr.substring(0, 10)
  return dateStr
}

module.exports = { formatTime, formatDate }
