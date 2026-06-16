import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'
import { getDict } from '@/utils/dictCache'

const STATUS_MAP = {
  0: '待提交',
  1: '审批中',
  2: '已通过',
  3: '已驳回',
  4: '已取消',
}

export const usePurchaseRequestStore = defineStore('purchaseRequest', () => {
  const items = ref([])
  const total = ref(0)
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const unitLabelMap = ref({})

  async function initUnitDict() {
    if (Object.keys(unitLabelMap.value).length > 0) return
    const items = await getDict('UNIT')
    const map = {}
    items.forEach(item => { map[item.code] = item.label })
    unitLabelMap.value = map
  }

  async function fetchList(params = {}) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/purchase-requests', { params })
      items.value = data.items
      total.value = data.total
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchDetail(id) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get(`/purchase-requests/${id}`)
      current.value = data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function create(formData) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.post('/purchase-requests', formData)
      return data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function update(id, formData) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.put(`/purchase-requests/${id}`, formData)
      current.value = data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function remove(id) {
    loading.value = true
    error.value = null
    try {
      await client.delete(`/purchase-requests/${id}`)
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function submitApproval(id, approverIds) {
    loading.value = true
    error.value = null
    try {
      await client.post('/tasks/start', {
        businessType: 'PURCHASE_REQUEST',
        businessId: id,
        variables: approverIds,
      })
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchApprovalHistory(id) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/tasks/history', {
        params: { businessType: 'PURCHASE_REQUEST', businessId: id }
      })
      return data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  function getUnitLabel(code) {
    if (code == null) return ''
    return unitLabelMap.value[code] || code
  }

  function getStatusLabel(code) {
    return STATUS_MAP[code] || '未知'
  }

  async function getUnitOptions() {
    const dict = await getDict('UNIT')
    return dict.map(d => ({ value: d.code, label: d.label }))
  }

  return {
    items,
    total,
    current,
    loading,
    error,
    unitLabelMap,
    initUnitDict,
    fetchList,
    fetchDetail,
    create,
    update,
    remove,
    submitApproval,
    fetchApprovalHistory,
    getUnitLabel,
    getStatusLabel,
    getUnitOptions,
  }
})
