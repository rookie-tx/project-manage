const auth = require('../../utils/auth')
const purchaseApi = require('../../api/purchase')

const UNITS = [
  { code: 0, label: '个' }, { code: 1, label: '台' },
  { code: 2, label: '套' }, { code: 3, label: '米' },
  { code: 4, label: '吨' }, { code: 5, label: '立方米' },
  { code: 6, label: '卷' }, { code: 7, label: '桶' },
  { code: 8, label: '箱' }, { code: 9, label: '其他' }
]

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

    // 单位选择器
    unitNames: UNITS.map(u => u.label),
    selectedUnitIdx: -1,

    recordCode: '',
    // 表单字段
    form: {
      projectId: null,
      name: '',
      specification: '',
      brand: '',
      quantity: '',
      unit: null,
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
    } catch { /* ignore */ }
  },

  async loadDetail(id) {
    try {
      const detail = await purchaseApi.detail(id)
      if (detail.statusCode !== 0) {
        wx.showToast({ title: '当前状态不可编辑', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1000)
        return
      }
      const projects = this.data.projects
      const projIdx = projects.findIndex(p => p.id === detail.projectId)
      const unitIdx = UNITS.findIndex(u => u.code === detail.unitCode)

      this.setData({
        recordCode: detail.code || '',
        selectedProjectIdx: projIdx,
        selectedUnitIdx: unitIdx,
        form: {
          projectId: detail.projectId,
          name: detail.name || '',
          specification: detail.specification || '',
          brand: detail.brand || '',
          quantity: detail.quantity != null ? String(detail.quantity) : '',
          unit: detail.unitCode != null ? detail.unitCode : null,
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

  onUnitChange(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ selectedUnitIdx: idx, 'form.unit': UNITS[idx].code })
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
    if (!form.name.trim()) { this.setData({ error: '请输入物料名称' }); return false }
    if (!form.quantity || parseFloat(form.quantity) <= 0) { this.setData({ error: '请输入有效数量' }); return false }
    if (form.unit == null) { this.setData({ error: '请选择单位' }); return false }
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
      name: form.name.trim(),
      specification: form.specification.trim() || null,
      brand: form.brand.trim() || null,
      quantity: parseFloat(form.quantity),
      unit: form.unit,
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
      // 通知列表页刷新
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
