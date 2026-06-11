import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

const STATUS_MAP = { 0: '采购中', 1: '已采购', 2: '已到货' }

export const usePurchaseOrderStore = defineStore('purchaseOrder', () => {
  const items = ref([])
  const total = ref(0)
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchList(params = {}) {
    loading.value = true; error.value = null
    try {
      const { data } = await client.get('/purchase-orders', { params })
      items.value = data.items; total.value = data.total
    } catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  async function fetchDetail(id) {
    loading.value = true; error.value = null
    try { const { data } = await client.get(`/purchase-orders/${id}`); current.value = data }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  async function create(formData) {
    loading.value = true; error.value = null
    try { const { data } = await client.post('/purchase-orders', formData); return data }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  async function update(id, formData) {
    loading.value = true; error.value = null
    try { const { data } = await client.put(`/purchase-orders/${id}`, formData); current.value = data }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  async function changeStatus(id, status) {
    loading.value = true; error.value = null
    try { const { data } = await client.put(`/purchase-orders/${id}/status`, { status }) }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  async function remove(id) {
    loading.value = true; error.value = null
    try { await client.delete(`/purchase-orders/${id}`) }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }

  function getStatusLabel(code) { return STATUS_MAP[code] || '未知' }

  return { items, total, current, loading, error, fetchList, fetchDetail, create, update, changeStatus, remove, getStatusLabel }
})
