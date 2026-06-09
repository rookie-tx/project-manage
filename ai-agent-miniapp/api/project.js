const client = require('./client')

function list(params) {
  return client.get('/projects', params)
}

function detail(id) {
  return client.get('/projects/' + id)
}

function create(payload) {
  return client.post('/projects', payload)
}

function update(id, payload) {
  return client.put('/projects/' + id, payload)
}

function del(id) {
  return client.del('/projects/' + id)
}

function getUsers() {
  return client.get('/users/select')
}

module.exports = { list, detail, create, update, del, getUsers }
