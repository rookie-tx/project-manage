/**
 * GPS 定位工具模块
 * - 封装 wx.getLocation，处理权限授权流程
 * - 提供模拟定位检测
 */

const LOCATION_RISK = {
  NORMAL: 0,
  SUSPICIOUS: 1,
  HIGH_RISK: 2,
  SEVERE: 3
}

/**
 * 获取当前 GPS 位置
 * 自动处理位置权限申请和引导
 * @returns {Promise<{lat: number, lng: number, accuracy: number, verticalAccuracy: number, speed: number, altitude: number, timestamp: number, risk: number}>}
 */
function getCurrentLocation() {
  return checkLocationPermission().then(() => {
    return new Promise((resolve, reject) => {
      wx.getLocation({
        type: 'gcj02',
        isHighAccuracy: true,
        highAccuracyExpireTime: 5000,
        success(res) {
          const risk = assessMockLocation(res)
          resolve({
            // 微信返回 5 位小数，统一转为 6 位与后端 DECIMAL(10,7) 对齐
            lat: roundCoord(res.latitude),
            lng: roundCoord(res.longitude),
            accuracy: res.accuracy || -1,
            verticalAccuracy: res.verticalAccuracy != null ? res.verticalAccuracy : -1,
            speed: res.speed != null ? res.speed : -1,
            altitude: res.altitude != null ? res.altitude : 0,
            timestamp: res.timestamp || Date.now(),
            risk: risk
          })
        },
        fail(err) {
          const msg = err.errMsg || ''
          if (msg.includes('auth deny') || msg.includes('authorize') || msg.includes('no permission')) {
            showOpenSettingDialog()
            reject(new Error('位置权限未开启，请在设置中允许访问位置信息'))
          } else {
            reject(new Error('获取位置失败，请检查定位服务是否开启'))
          }
        }
      })
    })
  })
}

/**
 * 检查并请求位置权限
 * 返回 Promise，权限就绪时 resolve，被拒绝时 reject
 */
function checkLocationPermission() {
  return new Promise((resolve, reject) => {
    wx.getSetting({
      success(res) {
        const auth = res.authSetting['scope.userLocation']
        if (auth === true) {
          // 已授权
          resolve()
        } else if (auth === false) {
          // 用户之前拒绝了，引导打开设置
          showOpenSettingDialog()
          reject(new Error('位置权限未开启，请在设置中允许访问位置信息'))
        } else {
          // 未请求过，主动申请
          wx.authorize({
            scope: 'scope.userLocation',
            success() {
              resolve()
            },
            fail() {
              showOpenSettingDialog()
              reject(new Error('位置权限未开启，请在设置中允许访问位置信息'))
            }
          })
        }
      },
      fail() {
        reject(new Error('获取权限状态失败，请检查微信版本'))
      }
    })
  })
}

/**
 * 弹出引导框，询问用户是否打开设置页开启位置权限
 */
function showOpenSettingDialog() {
  wx.showModal({
    title: '需要位置权限',
    content: '考勤打卡需要获取您的位置信息以校验是否在打卡范围内，请在设置中允许「位置信息」权限',
    confirmText: '去设置',
    cancelText: '取消',
    success(modalRes) {
      if (modalRes.confirm) {
        wx.openSetting({
          success(settingRes) {
            if (settingRes.authSetting['scope.userLocation']) {
              wx.showToast({ title: '权限已开启，请重新打卡', icon: 'none' })
            }
          }
        })
      }
    }
  })
}

/**
 * 模拟定位检测
 * @param {object} location wx.getLocation 返回的位置信息
 * @returns {number} 风险等级：0=正常, 1=可疑, 2=高风险, 3=严重
 */
/** 统一坐标精度到 6 位小数 */
function roundCoord(val) {
  return val != null ? Math.round(val * 1e6) / 1e6 : val
}

function assessMockLocation(location) {
  let risk = 0

  // 精度异常（>50米）
  if (location.accuracy > 50) {
    risk += 1
  }

  // 垂直精度异常（模拟器常返回负值）
  if (location.verticalAccuracy != null && location.verticalAccuracy < 0) {
    risk += 2
  }

  // 打卡时有移动速度
  if (location.speed > 0) {
    risk += 1
  }

  // 海拔恰好为 0（常见伪造特征）
  if (location.altitude === 0) {
    risk += 1
  }

  // 时间戳异常（与本地时间偏差超过5分钟）
  if (location.timestamp > 0) {
    const driftSec = Math.abs(Date.now() - location.timestamp) / 1000
    if (driftSec > 300) {
      risk += 1
    }
  }

  return Math.min(risk, 3)
}

module.exports = { getCurrentLocation, assessMockLocation, LOCATION_RISK }
