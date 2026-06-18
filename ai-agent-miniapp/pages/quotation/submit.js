/**
 * 报价表单页（提交/修改报价）
 */
const quotationApi = require('../../api/quotation')
const auth = require('../../utils/auth')

Page({
  data: {
    orderId: null,
    orderDetail: null,
    hasQuoted: false,
    error: '',
    form: {
      unitPrice: '',
      totalAmount: '',
      deliveryDate: '',
      remark: ''
    },
    submitting: false
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    const orderId = options.id
    this.setData({ orderId })
    this.loadOrderDetail(orderId)
  },

  async loadOrderDetail(orderId) {
    try {
      const result = await quotationApi.getPendingOrderDetail(orderId)
      const order = result.order
      const purchaseRequest = (order.purchaseRequests && order.purchaseRequests[0]) || {}
      this.setData({
        orderDetail: {
          id: order.id,
          materialName: purchaseRequest.name || '-',
          specification: purchaseRequest.specification || '-',
          quantityStr: purchaseRequest.quantityStr || '-',
          expectedArrivalDate: order.expectedArrivalDate || '-'
        },
        hasQuoted: result.hasQuoted
      })
      if (result.hasQuoted) {
        this.loadMyExistingQuotation()
      }
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
    }
  },

  async loadMyExistingQuotation() {
    try {
      const result = await quotationApi.listMyQuotations({ page: 1, size: 100 })
      const myQuote = (result.records || []).find(q => q.purchaseOrderId === parseInt(this.data.orderId))
      if (myQuote) {
        this.setData({
          form: {
            unitPrice: myQuote.unitPrice != null ? String(myQuote.unitPrice) : '',
            totalAmount: myQuote.totalAmount != null ? String(myQuote.totalAmount) : '',
            deliveryDate: myQuote.deliveryDate || '',
            remark: myQuote.remark || ''
          }
        })
      }
    } catch (e) { /* ignore */ }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    if (field) {
      this.setData({ ['form.' + field]: e.detail.value })
    }
  },

  onDateChange(e) {
    this.setData({ 'form.deliveryDate': e.detail.value })
  },

  async handleSubmit() {
    this.setData({ error: '' })
    const { unitPrice, totalAmount, deliveryDate } = this.data.form
    if (!totalAmount || parseFloat(totalAmount) <= 0) {
      this.setData({ error: '请填写有效总金额' })
      return
    }

    this.setData({ submitting: true })
    try {
      await quotationApi.submitQuotation({
        purchaseOrderId: parseInt(this.data.orderId),
        unitPrice: unitPrice ? parseFloat(unitPrice) : null,
        totalAmount: parseFloat(totalAmount),
        deliveryDate: deliveryDate || null,
        remark: this.data.form.remark || null
      })
      wx.showToast({ title: this.data.hasQuoted ? '报价已更新' : '报价成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch (e) {
      this.setData({ error: e.message || '提交失败' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
