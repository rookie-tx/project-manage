const client = require('./client')

/**
 * 供应商注册（公开接口，无需登录）
 * @param {object} payload - { code, companyName, licenseNo, contactPerson, contactPhone, address }
 */
function register(payload) {
  return client.post('/supplier/register', payload)
}

/**
 * 获取当前供应商资料（需登录）
 */
function getProfile() {
  return client.get('/supplier/my-profile')
}

/**
 * 更新当前供应商资料（需登录）
 * @param {object} payload - { contactPerson, contactPhone, address }
 */
function updateProfile(payload) {
  return client.put('/supplier/my-profile', payload)
}

module.exports = { register, getProfile, updateProfile }
