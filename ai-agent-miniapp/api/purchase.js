const client = require('./client')

/**
 * 查询采购申请列表
 * @param {object} params - { projectId, page, size }
 */
function list(params) {
  return client.get('/purchase-requests', params)
}

/**
 * 查询采购申请详情
 * @param {number} id
 */
function detail(id) {
  return client.get('/purchase-requests/' + id)
}

/**
 * 创建采购申请
 * @param {object} payload
 */
function create(payload) {
  return client.post('/purchase-requests', payload)
}

/**
 * 更新采购申请
 * @param {number} id
 * @param {object} payload
 */
function update(id, payload) {
  return client.put('/purchase-requests/' + id, payload)
}

/**
 * 删除采购申请（软删除）
 * @param {number} id
 */
function del(id) {
  return client.del('/purchase-requests/' + id)
}

/**
 * 获取项目下拉列表
 */
function getProjects() {
  return client.get('/projects/select')
}

/**
 * 获取用户下拉列表（审批人选择用）
 */
function getUsers() {
  return client.get('/users/select')
}

module.exports = { list, detail, create, update, del, getProjects, getUsers }
