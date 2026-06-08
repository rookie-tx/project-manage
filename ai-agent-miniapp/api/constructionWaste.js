const client = require('./client')

/**
 * 查询废渣运出记录列表
 * @param {object} params - { projectId, wasteType, startDate, endDate, offset, limit }
 */
function list(params) {
  return client.get('/construction-wastes', params)
}

/**
 * 查询废渣运出记录详情
 * @param {number} id
 */
function getById(id) {
  return client.get('/construction-wastes/' + id)
}

/**
 * 创建废渣运出记录
 * @param {object} payload - { projectId, wasteType, quantity, unit, vehiclePlate, driverName, destination, removalDate, remark, fileIds }
 */
function create(payload) {
  return client.post('/construction-wastes', payload)
}

/**
 * 删除废渣运出记录（软删除）
 * @param {number} id
 */
function del(id) {
  return client.del('/construction-wastes/' + id)
}

/**
 * 获取项目下拉列表（供选择器使用）
 */
function getProjects() {
  return client.get('/projects/select')
}

module.exports = { list, getById, create, del, getProjects }
