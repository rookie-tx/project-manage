/**
 * 报价管理 API 模块
 */
const client = require('./client')

/** 待报价采购单列表 */
function listPendingOrders(params) {
  return client.get('/supplier/pending-orders', params)
}

/** 待报价采购单详情 */
function getPendingOrderDetail(id) {
  return client.get('/supplier/pending-orders/' + id)
}

/** 提交报价 */
function submitQuotation(payload) {
  return client.post('/supplier/quotations', payload)
}

/** 我的报价列表 */
function listMyQuotations(params) {
  return client.get('/supplier/quotations', params)
}

/** 我的报价详情 */
function getMyQuotationDetail(id) {
  return client.get('/supplier/quotations/' + id)
}

module.exports = {
  listPendingOrders,
  getPendingOrderDetail,
  submitQuotation,
  listMyQuotations,
  getMyQuotationDetail
}
