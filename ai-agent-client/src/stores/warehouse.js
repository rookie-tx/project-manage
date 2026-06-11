import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useWarehouseStore = defineStore('warehouse', () => {
  const warehouses = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchAll() {
    loading.value = true; error.value = null
    try { const { data } = await client.get('/warehouses'); warehouses.value = data }
    catch (e) { error.value = e.message; throw e }
    finally { loading.value = false }
  }
  async function create(payload) { await client.post('/warehouses', payload) }
  async function update(id, payload) { await client.put(`/warehouses/${id}`, payload) }
  async function remove(id) { await client.delete(`/warehouses/${id}`) }
  async function fetchStock(id) { const { data } = await client.get(`/warehouses/${id}/stock`); return data }

  return { warehouses, loading, error, fetchAll, create, update, remove, fetchStock }
})

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([])
  const loading = ref(false)

  async function fetchAll() { loading.value = true; try { const { data } = await client.get('/materials'); materials.value = data } finally { loading.value = false } }
  async function create(payload) { await client.post('/materials', payload) }
  async function update(id, payload) { await client.put(`/materials/${id}`, payload) }
  async function remove(id) { await client.delete(`/materials/${id}`) }
  async function fetchStock(id) { const { data } = await client.get(`/materials/${id}/stock`); return data }

  return { materials, loading, fetchAll, create, update, remove, fetchStock }
})

export const useStockRecordStore = defineStore('stockRecord', () => {
  const records = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchAll(params = {}) {
    loading.value = true
    try {
      const { data } = await client.get('/stock-records', {
        params: { ...params, page: params.page || 1, size: params.size || 20 }
      })
      records.value = Array.isArray(data) ? data : []
      total.value = 0
    } finally { loading.value = false }
  }
  async function createStock(payload) {
    if (payload.type === 2) {
      if (!confirm(`确认出库 ${payload.quantity} 单位？`)) return
    }
    await client.post('/stock-records', payload)
  }

  return { records, total, loading, fetchAll, createStock }
})
