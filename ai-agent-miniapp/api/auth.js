const client = require('./client')

function wechatLogin(code) {
  return client.post('/auth/wechat-login', { code: code })
}

function wechatBind(code, username, password) {
  return client.post('/auth/wechat-bind', { code: code, username: username, password: password })
}

function wechatUnbind() {
  return client.del('/auth/wechat-unbind')
}

module.exports = { wechatLogin, wechatBind, wechatUnbind }
