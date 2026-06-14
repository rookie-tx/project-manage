const auth = require('../../utils/auth')
const client = require('../../api/client')

Page({
  data: {
    id: null, prId: null,
    warehouses: [], materials: [],
    form: { warehouseId: null, materialId: null, quantity: 1 },
    warehouseNames: [], materialNames: [],
    wIdx: -1, mIdx: -1,
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
      const [wRes, mRes] = await Promise.all([
        client.get('/warehouses'),
        client.get('/materials')
      ])
      const warehouses = wRes || []
      const materials = mRes || []
      this.setData({
        warehouses,
        materials,
        warehouseNames: warehouses.map(w => w.name),
        materialNames: materials.map(m => m.name)
      })
    } catch (e) { /* ignore */ }
  },

  onWChange(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ wIdx: idx, 'form.warehouseId': this.data.warehouses[idx]?.id })
  },
  onMChange(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ mIdx: idx, 'form.materialId': this.data.materials[idx]?.id })
  },
  onQtyInput(e) { this.setData({ 'form.quantity': Number(e.detail.value) || 0 }) },

  async handleSubmit() {
    const f = this.data.form
    if (!f.warehouseId) { this.setData({ error: '请选择仓库' }); return }
    if (!f.materialId) { this.setData({ error: '请选择物料' }); return }
    if (!f.quantity || f.quantity <= 0) { this.setData({ error: '请输入有效数量' }); return }
    this.setData({ submitting: true, error: '' })
    try {
      await client.post('/stock-records', {
        type: 1,
        warehouseId: f.warehouseId,
        materialId: f.materialId,
        quantity: f.quantity,
        purchaseRequestId: this.data.prId
      })
      wx.showToast({ title: '入库完成', icon: 'success' })
      // 刷新上级页面（采购订单列表或详情）
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
