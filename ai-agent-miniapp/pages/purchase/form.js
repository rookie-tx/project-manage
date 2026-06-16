const auth = require('../../utils/auth')
const purchaseApi = require('../../api/purchase')
const client = require('../../api/client')

Page({
  data: {
    isEdit: false,
    editId: null,
    title: '新建采购申请',
    submitting: false,
    error: '',

    // 项目选择器
    projects: [],
    projectNames: [],
    selectedProjectIdx: -1,

    // 物料选择器
    materials: [],
    materialNames: [],
    selectedMaterialIdx: -1,

    // 规格和单位（只读，物料自动带出）
    materialSpec: '',
    materialUnitLabel: '',

    recordCode: '',
    // 表单字段
    form: {
      projectId: null,
      materialId: null,
      brand: '',
      quantity: '',
      arrivalDate: '',
      remark: ''
    }
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadProjects()
    this.loadMaterials()

    if (options.id) {
      this.setData({ isEdit: true, editId: parseInt(options.id), title: '编辑采购申请' })
      this.loadDetail(options.id)
    }
  },

  async loadProjects() {
    try {
      const projects = await purchaseApi.getProjects()
      this.setData({
        projects: projects || [],
        projectNames: (projects || []).map(p => p.name)
      })
    } catch (e) { /* ignore */ }
  },

  async loadMaterials() {
    try {
      const res = await client.get('/materials')
      const materials = res || []
      this.setData({
        materials: materials,
        materialNames: materials.map(m => `${m.code} ${m.name}`)
      })
    } catch (e) { /* ignore */ }
  },

  async loadDetail(id) {
    try {
      const detail = await purchaseApi.detail(id)
      if (detail.statusCode !== 0) {
        wx.showToast({ title: '当前状态不可编辑', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1000)
        return
      }
      const projIdx = this.data.projects.findIndex(p => p.id === detail.projectId)
      const matIdx = this.data.materials.findIndex(m => m.id === detail.materialId)

      this.setData({
        recordCode: detail.code || '',
        selectedProjectIdx: projIdx,
        selectedMaterialIdx: matIdx,
        materialSpec: detail.materialSpec || detail.specification || '',
        materialUnitLabel: detail.materialUnit || '',
        form: {
          projectId: detail.projectId,
          materialId: detail.materialId,
          brand: detail.brand || '',
          quantity: detail.quantity != null ? String(detail.quantity) : '',
          arrivalDate: detail.arrivalDate || '',
          remark: detail.remark || ''
        }
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
    }
  },

  onProjectChange(e) {
    const idx = parseInt(e.detail.value)
    const project = this.data.projects[idx]
    this.setData({ selectedProjectIdx: idx, 'form.projectId': project ? project.id : null })
  },

  onMaterialChange(e) {
    const idx = parseInt(e.detail.value)
    const material = this.data.materials[idx]
    if (material) {
      const unitLabels = ['个', '台', '套', '米', '吨', '立方米', '卷', '桶', '箱', '其他']
      this.setData({
        selectedMaterialIdx: idx,
        'form.materialId': material.id,
        materialSpec: material.spec || '',
        materialUnitLabel: material.unit != null ? (unitLabels[material.unit] || '未知') : ''
      })
    }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  onDateChange(e) {
    this.setData({ 'form.arrivalDate': e.detail.value })
  },

  validate() {
    const { form } = this.data
    if (!form.projectId) { this.setData({ error: '请选择项目' }); return false }
    if (!form.materialId) { this.setData({ error: '请选择物料' }); return false }
    if (!form.quantity || parseFloat(form.quantity) <= 0) { this.setData({ error: '请输入有效数量' }); return false }
    if (!form.arrivalDate) { this.setData({ error: '请选择到场时间' }); return false }
    return true
  },

  async handleSubmit() {
    this.setData({ error: '' })
    if (!this.validate()) return
    this.setData({ submitting: true })

    const { form, isEdit, editId } = this.data
    const payload = {
      projectId: form.projectId,
      materialId: form.materialId,
      brand: form.brand.trim() || null,
      quantity: parseFloat(form.quantity),
      arrivalDate: form.arrivalDate,
      remark: form.remark.trim() || null
    }

    try {
      if (isEdit) {
        await purchaseApi.update(editId, payload)
      } else {
        await purchaseApi.create(payload)
      }
      wx.showToast({ title: isEdit ? '修改成功' : '创建成功', icon: 'success' })
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage) prevPage.setData({ needRefresh: true })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
