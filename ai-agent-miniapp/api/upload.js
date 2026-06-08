const auth = require('../utils/auth')
const storage = require('../utils/storage')

const BASE_URL = 'https://legwork-blush-vitally.ngrok-free.dev/api'

/**
 * 上传单个文件到后端
 * @param {string} filePath - wx.chooseImage 返回的临时文件路径
 * @param {string} businessType - 业务类型（如 'construction_waste'）
 * @returns {Promise<number>} 返回上传成功后的 fileId
 */
function uploadFile(filePath, businessType) {
  // 双重保障：内存优先，storage 兜底
  let token = auth.getToken()
  if (!token) {
    token = storage.getToken()
  }

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + '/files/upload',
      filePath: filePath,
      name: 'file',
      formData: businessType ? { businessType: businessType } : {},
      header: {
        'Authorization': token ? 'Bearer ' + token : '',
        'ngrok-skip-browser-warning': '69420'
      },
      success(res) {
        if (res.statusCode !== 200) {
          reject(new Error('上传失败: ' + res.statusCode))
          return
        }
        try {
          const body = JSON.parse(res.data)
          if (body.code === 0) {
            resolve(body.data.id)
            return
          }
          // 认证错误 → 跳转登录（与 client.js 保持一致）
          if (body.code === 2001 || body.code === 2004) {
            auth.clearAuth()
            wx.reLaunch({ url: '/pages/login/login' })
            reject(new Error('登录已过期，请重新登录'))
            return
          }
          reject(new Error(body.msg || '上传失败'))
        } catch {
          reject(new Error('服务器响应异常'))
        }
      },
      fail(err) {
        reject(new Error('网络连接失败，请检查网络'))
      }
    })
  })
}

/**
 * 并发上传多个文件
 * @param {string[]} filePaths - 临时文件路径数组
 * @param {string} businessType - 业务类型
 * @returns {Promise<number[]>} 返回所有上传成功后的 fileId 数组
 */
function uploadFiles(filePaths, businessType) {
  if (!filePaths || filePaths.length === 0) {
    return Promise.resolve([])
  }
  const tasks = filePaths.map(fp => uploadFile(fp, businessType))
  return Promise.all(tasks)
}

module.exports = { uploadFile, uploadFiles }
