const storage = require('./storage')

let _token = storage.getToken()
let _user = storage.getUser()

function isLoggedIn() {
  return !!_token
}

function getToken() {
  return _token
}

function getUser() {
  return _user
}

function setAuth(token, user) {
  _token = token
  _user = user
  storage.setToken(token)
  storage.setUser(user)
}

function clearAuth() {
  _token = null
  _user = null
  storage.clearAuth()
}

function logout() {
  clearAuth()
  wx.reLaunch({ url: '/pages/login/login' })
}

module.exports = { isLoggedIn, getToken, getUser, setAuth, clearAuth, logout }
