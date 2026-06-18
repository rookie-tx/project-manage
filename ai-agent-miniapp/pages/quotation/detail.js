/**
 * 报价详情页
 */
const quotationApi = require('../../api/quotation')
const auth = require('../../utils/auth')

Page({
  data: {
    detail: null,
    loading: true
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    const id = options.id
    this.loadDetail(id)
  },

  async loadDetail(id) {
    this.setData({ loading: true })
    try {
      const result = await quotationApi.getMyQuotationDetail(id)
      this.setData({
        detail: {
          id: result.id,
          purchaseOrderId: result.purchaseOrderId,
          supplierCompanyName: result.supplierCompanyName || '-',
          supplierContactPerson: result.supplierContactPerson || '-',
          supplierContactPhone: result.supplierContactPhone || '-',
          unitPrice: result.unitPrice,
          totalAmount: result.totalAmount,
          deliveryDate: result.deliveryDate || '-',
          remark: result.remark || '-',
          statusCode: result.statusCode,
          statusLabel: result.statusLabel,
          createdByName: result.createdByName || '-',
          createdAt: result.createdAt ? result.createdAt.substring(0, 16).replace('T', ' ') : '-',
          updatedAt: result.updatedAt ? result.updatedAt.substring(0, 16).replace('T', ' ') : '-',
          materialName: (result.purchaseOrderInfo && result.purchaseOrderInfo.materialName) ? result.purchaseOrderInfo.materialName : '-',
          materialSpec: (result.purchaseOrderInfo && result.purchaseOrderInfo.materialSpec) ? result.purchaseOrderInfo.materialSpec : '-',
          quantityStr: (result.purchaseOrderInfo && result.purchaseOrderInfo.quantityStr) ? result.purchaseOrderInfo.quantityStr : '-',
          projectName: (result.purchaseOrderInfo && result.purchaseOrderInfo.projectName) ? result.purchaseOrderInfo.projectName : '-'
        },
        loading: false
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
      this.setData({ loading: false })
    }
  }
})
