const auth = require('../../utils/auth')
const location = require('../../utils/location')
const attendanceApi = require('../../api/attendance')

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六']

Page({
  data: {
    username: '',
    currentDate: '',
    currentMonth: '',
    // 今日打卡
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
    recordsLoading: false
  },

  onLoad() {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = auth.getUser()
    const now = new Date()
    const m = now.getMonth() + 1
    this.setData({
      username: user ? (user.name || user.username) : '未知',
      currentDate: now.getFullYear() + '年' + m + '月' + now.getDate() + '日',
      currentMonth: now.getFullYear() + '-' + (m < 10 ? '0' + m : '' + m)
    })
    this.fetchToday()
    this.fetchRecords()
  },

  async fetchToday() {
    try {
      const records = await attendanceApi.getMyRecords(this.data.currentMonth)
      if (records && records.length > 0) {
        const today = new Date().toISOString().substring(0, 10)
        const todayRecord = records.find(r => r.clockIn && r.clockIn.startsWith(today))
        if (todayRecord) {
          this.setData({
            todayRecord,
            isClockedIn: true,
            isClockedOut: todayRecord.status === 'CLOCKED_OUT',
            clockInTime: formatTime(todayRecord.clockIn),
            clockOutTime: todayRecord.clockOut ? formatTime(todayRecord.clockOut) : ''
          })
          return
        }
      }
      this.setData({ todayRecord: null, isClockedIn: false, isClockedOut: false, clockInTime: '', clockOutTime: '' })
    } catch (e) {
      this.setData({ todayRecord: null })
    }
  },

  async fetchRecords() {
    this.setData({ recordsLoading: true })
    try {
      const records = await attendanceApi.getMyRecords(this.data.currentMonth)
      const formatted = (records || []).map(r => {
        const d = r.clockIn ? new Date(r.clockIn.substring(0, 10) + 'T00:00:00') : null
        return {
          id: r.id,
          dateStr: formatDate(r.clockIn),
          weekday: d ? '周' + WEEKDAYS[d.getDay()] : '',
          clockInStr: formatTime(r.clockIn),
          clockOutStr: formatTime(r.clockOut),
          isDone: r.status === 'CLOCKED_OUT'
        }
      })
      this.setData({ records: formatted })
    } catch (e) {
      this.setData({ records: [] })
    } finally {
      this.setData({ recordsLoading: false })
    }
  },

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

function formatTime(dateStr) {
  if (!dateStr) return '--:--'
  const tIdx = dateStr.indexOf('T')
  if (tIdx >= 0) return dateStr.substring(tIdx + 1, tIdx + 6)
  if (dateStr.length >= 16) return dateStr.substring(11, 16)
  return '--:--'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  if (dateStr.length >= 10) return dateStr.substring(0, 10)
  return dateStr
}
