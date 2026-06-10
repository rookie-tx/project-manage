const client = require('./client')

function list(params) { return client.get('/purchase-orders', params) }
function detail(id) { return client.get('/purchase-orders/' + id) }
function create(payload) { return client.post('/purchase-orders', payload) }
function update(id, payload) { return client.put('/purchase-orders/' + id, payload) }
function changeStatus(id, status) { return client.put('/purchase-orders/' + id + '/status', { status }) }
function del(id) { return client.del('/purchase-orders/' + id) }
function getAvailablePRs() { return client.get('/purchase-requests/available') }
function getApprovedPRs() { return client.get('/purchase-requests', { status: 2 }) }
function getProjects() { return client.get('/projects/select') }

module.exports = { list, detail, create, update, changeStatus, del, getAvailablePRs, getApprovedPRs, getProjects }
