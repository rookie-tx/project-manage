const client = require('./client')

/**
 * 查询物料列表
 */
function list() {
  return client.get('/materials')
}

/**
 * 查询物料详情
 * @param {number} id
 */
function detail(id) {
  return client.get('/materials/' + id)
}

/**
 * 创建物料
 * @param {object} payload
 */
function create(payload) {
  return client.post('/materials', payload)
}

/**
 * 更新物料
 * @param {number} id
 * @param {object} payload
 */
function update(id, payload) {
  return client.put('/materials/' + id, payload)
}

/**
 * 删除物料
 * @param {number} id
 */
function del(id) {
  return client.del('/materials/' + id)
}

module.exports = { list, detail, create, update, del }
