const auth = require('../../utils/auth')
const projectApi = require('../../api/project')

const TYPES = ['房建', '市政', '装修', '路桥', '水利', '其他']
const STATUSES = ['筹备中', '在建', '停工', '已竣工', '已结算']

Page({
  data: {
    isEdit: false, editId: null, title: '新建项目',
    submitting: false, error: '',
    typeNames: TYPES, typeIdx: -1,
    statusNames: STATUSES, statusIdx: -1,
    users: [], userNames: [], mgrIdx: -1,
    form: {
      name: '', location: '', clientName: '', supervisorUnit: '',
      projectManagerId: null, plannedStartDate: '', plannedEndDate: '',
      remark: '', type: null, status: null
    }
  },

  onLoad(options) {
    if (!auth.isLoggedIn()) { wx.reLaunch({ url: '/pages/login/login' }); return }
    this.loadUsers()
    if (options.id) {
      this.setData({ isEdit: true, editId: parseInt(options.id), title: '编辑项目' })
      this.loadDetail(options.id)
    }
  },

  async loadUsers() {
    try {
      const users = await projectApi.getUsers()
      this.setData({ users: users || [], userNames: (users || []).map(u => u.username) })
    } catch { /* ignore */ }
  },

  async loadDetail(id) {
    try {
      const d = await projectApi.detail(id)
      const mgrIdx = this.data.users.findIndex(u => u.id === d.projectManagerId)
      this.setData({
        typeIdx: d.type != null ? d.type > 4 ? 5 : d.type : -1,
        statusIdx: d.status || 0,
        mgrIdx: mgrIdx,
        form: {
          name: d.name || '', location: d.location || '', clientName: d.clientName || '',
          supervisorUnit: d.supervisorUnit || '', projectManagerId: d.projectManagerId,
          plannedStartDate: d.plannedStartDate || '', plannedEndDate: d.plannedEndDate || '',
          remark: d.remark || '', type: d.type, status: d.status
        }
      })
    } catch (e) {
      wx.showToast({ title: e.message || '加载失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
    }
  },

  onTypeChange(e) { const idx = parseInt(e.detail.value); this.setData({ typeIdx: idx, 'form.type': idx > 4 ? 9 : idx }) },
  onStatusChange(e) { const idx = parseInt(e.detail.value); this.setData({ statusIdx: idx, 'form.status': idx }) },
  onMgrChange(e) { const idx = parseInt(e.detail.value); this.setData({ mgrIdx: idx, 'form.projectManagerId': this.data.users[idx]?.id || null }) },
  onInput(e) { this.setData({ ['form.' + e.currentTarget.dataset.field]: e.detail.value }) },
  onStartDateChange(e) { this.setData({ 'form.plannedStartDate': e.detail.value }) },
  onEndDateChange(e) { this.setData({ 'form.plannedEndDate': e.detail.value }) },

  validate() {
    const f = this.data.form
    if (!f.name.trim()) { this.setData({ error: '请输入项目名称' }); return false }
    if (f.type == null) { this.setData({ error: '请选择项目类型' }); return false }
    if (!f.projectManagerId) { this.setData({ error: '请选择项目经理' }); return false }
    if (!f.plannedStartDate) { this.setData({ error: '请选择计划开工日期' }); return false }
    if (!f.plannedEndDate) { this.setData({ error: '请选择计划竣工日期' }); return false }
    return true
  },

  async handleSubmit() {
    this.setData({ error: '' })
    if (!this.validate()) return
    this.setData({ submitting: true })

    const f = this.data.form
    const payload = {
      name: f.name.trim(), type: f.type,
      location: f.location.trim() || null, clientName: f.clientName.trim() || null,
      supervisorUnit: f.supervisorUnit.trim() || null, projectManagerId: f.projectManagerId,
      plannedStartDate: f.plannedStartDate, plannedEndDate: f.plannedEndDate,
      remark: f.remark.trim() || null
    }
    if (this.data.isEdit) payload.status = f.status

    try {
      if (this.data.isEdit) {
        await projectApi.update(this.data.editId, payload)
      } else {
        await projectApi.create(payload)
      }
      wx.showToast({ title: this.data.isEdit ? '修改成功' : '创建成功', icon: 'success' })
      const pages = getCurrentPages(); const prev = pages[pages.length - 2]
      if (prev) prev.setData({ needRefresh: true })
      setTimeout(() => wx.navigateBack(), 1000)
    } catch (e) {
      wx.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
