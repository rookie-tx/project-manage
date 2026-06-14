const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || null
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

function removeToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

function getUser() {
  try {
    const raw = wx.getStorageSync(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

function setUser(user) {
  wx.setStorageSync(USER_KEY, JSON.stringify(user))
}

function removeUser() {
  wx.removeStorageSync(USER_KEY)
}

function clearAuth() {
  removeToken()
  removeUser()
}

module.exports = { getToken, setToken, removeToken, getUser, setUser, removeUser, clearAuth }
