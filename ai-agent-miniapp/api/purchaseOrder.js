const client = require('./client')

/**
 * 采购记录列表（支持 tab/status 筛选）
 * @param {object} params - { tab, status, page, size }
 */
function list(params) { return client.get('/purchase-orders', params) }

/**
 * 采购记录详情
 * @param {number} id
 */
function detail(id) { return client.get('/purchase-orders/' + id) }

/**
 * 更新采购记录（补填供应商信息）
 * @param {number} id
 * @param {object} payload
 */
function update(id, payload) { return client.put('/purchase-orders/' + id, payload) }

/**
 * 变更采购记录状态
 * @param {number} id
 * @param {number} status - 1=已采购, 2=已到货
 */
function changeStatus(id, status) { return client.put('/purchase-orders/' + id + '/status', { status }) }

/**
 * 待入库 PO 列表（status=已采购）
 */
function pendingInbound() { return client.get('/purchase-orders/pending-inbound') }

/**
 * 获取采购单的报价列表
 * @param {number} orderId
 */
function fetchQuotations(orderId) { return client.get('/purchase-orders/' + orderId + '/quotations') }

/**
 * 选用报价
 * @param {number} orderId
 * @param {number} quotationId
 */
function selectQuotation(orderId, quotationId) { return client.post('/purchase-orders/' + orderId + '/quotations/' + quotationId + '/select') }

module.exports = { list, detail, update, changeStatus, pendingInbound, fetchQuotations, selectQuotation }
