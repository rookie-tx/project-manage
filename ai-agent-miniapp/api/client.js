const auth = require('../utils/auth')
const storage = require('../utils/storage')

const BASE_URL = 'https://legwork-blush-vitally.ngrok-free.dev/api'

/**
 * 统一请求封装
 * - 自动附加 Authorization: Bearer <token>
 * - code === 0 解包 data
 * - 认证错误跳转登录
 */
function request(method, url, data) {
  // 双重保障：内存优先，storage 兜底（防止模块变量因小程序引擎回收而丢失）
  let token = auth.getToken()
  if (!token) {
    token = storage.getToken()
  }
  const header = {
    'Content-Type': 'application/json',
    'ngrok-skip-browser-warning': '69420'
  }
  if (token) {
    header['Authorization'] = 'Bearer ' + token
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: method,
      data: data,
      header: header,
      success(res) {
        if (res.statusCode !== 200) {
          reject(new Error('服务器错误: ' + res.statusCode))
          return
        }
        const body = res.data
        if (body.code === 0) {
          resolve(body.data)
          return
        }
        // 认证错误 → 跳转登录
        if (body.code === 2001 || body.code === 2004) {
          auth.clearAuth()
          wx.reLaunch({ url: '/pages/login/login' })
        }
        reject(new Error(body.msg || '请求失败'))
      },
      fail(err) {
        reject(new Error('网络连接失败，请检查网络'))
      }
    })
  })
}

module.exports = {
  get(url, params) {
    let query = ''
    if (params) {
      const parts = []
      for (const key of Object.keys(params)) {
        const v = params[key]
        if (v != null && v !== '') {
          parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(v))
        }
      }
      if (parts.length) query = '?' + parts.join('&')
    }
    return request('GET', url + query)
  },
  post(url, data) {
    return request('POST', url, data)
  },
  del(url, data) {
    return request('DELETE', url, data)
  }
}
