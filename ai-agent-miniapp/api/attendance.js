const client = require('./client')

/**
 * 上班打卡
 * @param {object} location GPS 位置信息 {lat, lng, accuracy, verticalAccuracy, speed, altitude, timestamp}
 */
function clockIn(location) {
  return client.post('/attendance/clock-in', {
    lat: location.lat,
    lng: location.lng,
    accuracy: location.accuracy,
    verticalAccuracy: location.verticalAccuracy,
    speed: location.speed,
    altitude: location.altitude,
    locationTimestamp: location.timestamp
  })
}

/**
 * 下班打卡
 * @param {object} location GPS 位置信息
 */
function clockOut(location) {
  return client.post('/attendance/clock-out', {
    lat: location.lat,
    lng: location.lng,
    accuracy: location.accuracy,
    verticalAccuracy: location.verticalAccuracy,
    speed: location.speed,
    altitude: location.altitude,
    locationTimestamp: location.timestamp
  })
}

/**
 * 查询当前用户本月打卡记录
 * @param {string} yearMonth 月份，如 '2026-06'，默认当月
 */
function getMyRecords(yearMonth) {
  const q = yearMonth ? '?yearMonth=' + encodeURIComponent(yearMonth) : ''
  return client.get('/attendance/my' + q)
}

module.exports = { clockIn, clockOut, getMyRecords }
