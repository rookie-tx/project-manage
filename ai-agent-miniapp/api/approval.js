const client = require('./client')

/**
 * 查询待办任务列表
 * @param {object} params - { page, size }
 */
function list(params) {
  return client.get('/tasks', params)
}

/**
 * 查询待办任务数量
 */
function count() {
  return client.get('/tasks/count')
}

/**
 * 审批处理（通过/驳回）
 * @param {string} taskId
 * @param {boolean} approved
 * @param {string} comment
 */
function complete(taskId, approved, comment) {
  return client.post('/tasks/' + taskId + '/complete', {
    approved: approved,
    comment: comment || null
  })
}

/**
 * 查询审批历史
 * @param {string} businessType
 * @param {number} businessId
 */
function history(businessType, businessId) {
  return client.get('/tasks/history', { businessType: businessType, businessId: businessId })
}

module.exports = { list, count, complete, history }
