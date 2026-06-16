const auth = require('../../utils/auth')
const materialApi = require('../../api/material')

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
    title: '新建物料',
    submitting: false,
    error: '',

    unitNames: UNITS.map(u => u.label),
    selectedUnitIdx: -1,

    form: {
      code: '',
      name: '',
      spec: '',
      unit: null
    }
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    if (options.id) {
      this.setData({ isEdit: true, editId: parseInt(options.id), title: '编辑物料' })
      this.loadDetail(options.id)
    }
  },

  async loadDetail(id) {
    try {
      const detail = await materialApi.detail(id)
      const unitIdx = UNITS.findIndex(u => u.code === detail.unit)
      this.setData({
        selectedUnitIdx: unitIdx,
        form: {
          code: detail.code || '',
          name: detail.name || '',
          spec: detail.spec || '',
          unit: detail.unit != null ? detail.unit : null
        }
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
    }
  },

  onUnitChange(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ selectedUnitIdx: idx, 'form.unit': UNITS[idx].code })
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  validate() {
    const { form, isEdit } = this.data
    if (!isEdit && !form.code.trim()) { this.setData({ error: '请输入物料编码' }); return false }
    if (!form.name.trim()) { this.setData({ error: '请输入物料名称' }); return false }
    if (!form.spec.trim()) { this.setData({ error: '请输入规格型号' }); return false }
    if (form.unit == null) { this.setData({ error: '请选择单位' }); return false }
    return true
  },

  async handleSubmit() {
    this.setData({ error: '' })
    if (!this.validate()) return
    this.setData({ submitting: true })

    const { form, isEdit, editId } = this.data
    const payload = {
      code: form.code.trim(),
      name: form.name.trim(),
      spec: form.spec.trim(),
      unit: form.unit
    }

    try {
      if (isEdit) {
        await materialApi.update(editId, payload)
      } else {
        await materialApi.create(payload)
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
