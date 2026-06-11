import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

const UNIT_MAP = {
  0: '个',
  1: '台',
  2: '套',
  3: '米',
  4: '吨',
  5: '立方米',
  6: '卷',
  7: '桶',
  8: '箱',
  9: '其他',
}

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
    return UNIT_MAP[code] || '未知'
  }

  function getStatusLabel(code) {
    return STATUS_MAP[code] || '未知'
  }

  function getUnitOptions() {
    return Object.entries(UNIT_MAP).map(([value, label]) => ({
      value: Number(value),
      label,
    }))
  }

  return {
    items,
    total,
    current,
    loading,
    error,
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
