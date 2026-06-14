import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useSupplierStore = defineStore('supplier', () => {
  const records = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)

  async function fetchList(params = {}) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/suppliers', {
        params: { page: params.page || 1, size: params.size || 20, keyword: params.keyword, status: params.status }
      })
      records.value = data.records || []
      total.value = data.total || 0
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function create(payload) {
    await client.post('/suppliers', payload)
  }

  async function update(id, payload) {
    await client.put(`/suppliers/${id}`, payload)
  }

  async function remove(id) {
    await client.delete(`/suppliers/${id}`)
  }

  async function fetchDetail(id) {
    const { data } = await client.get(`/suppliers/${id}`)
    return data
  }

  return { records, total, loading, error, fetchList, create, update, remove, fetchDetail }
})
