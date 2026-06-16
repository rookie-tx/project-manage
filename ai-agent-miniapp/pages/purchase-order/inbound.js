const auth = require('../../utils/auth')
const client = require('../../api/client')

Page({
  data: {
    id: null, prId: null,
    warehouses: [],
    // 物料信息从采购申请自动带入，只读
    materialId: null,
    materialLabel: '',
    materialSpec: '',
    materialUnit: '',
    form: { warehouseId: null, quantity: 0 },
    warehouseNames: [],
    wIdx: -1,
    submitting: false, error: ''
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.setData({
      id: options.id ? parseInt(options.id) : null,
      prId: options.prId ? parseInt(options.prId) : null
    })
    this.loadData()
  },

  async loadData() {
    try {
      const [wRes, prDetail] = await Promise.all([
        client.get('/warehouses'),
        this.data.prId ? client.get('/purchase-requests/' + this.data.prId) : Promise.resolve(null)
      ])
      const warehouses = wRes || []
      this.setData({
        warehouses,
        warehouseNames: warehouses.map(w => w.name),
        materialId: prDetail ? prDetail.materialId : null,
        materialLabel: prDetail ? (prDetail.materialName || prDetail.name || '') : '',
        materialSpec: prDetail ? (prDetail.materialSpec || prDetail.specification || '') : '',
        materialUnit: prDetail ? (prDetail.materialUnit || '') : '',
        'form.quantity': prDetail ? prDetail.quantity : 0
      })
    } catch (e) { /* ignore */ }
  },

  onWChange(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ wIdx: idx, 'form.warehouseId': this.data.warehouses[idx]?.id })
  },

  async handleSubmit() {
    const f = this.data.form
    if (!f.warehouseId) { this.setData({ error: '请选择仓库' }); return }
    if (!this.data.materialId) { this.setData({ error: '采购申请未关联物料' }); return }
    if (!f.quantity || f.quantity <= 0) { this.setData({ error: '请输入有效数量' }); return }
    this.setData({ submitting: true, error: '' })
    try {
      await client.post('/stock-records', {
        type: 1,
        warehouseId: f.warehouseId,
        materialId: this.data.materialId,
        quantity: f.quantity,
        purchaseRequestId: this.data.prId
      })
      wx.showToast({ title: '入库完成', icon: 'success' })
      const pages = getCurrentPages()
      const prev = pages[pages.length - 2]
      if (prev) prev.setData({ needRefresh: true })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      this.setData({ error: e.message || '入库失败' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
