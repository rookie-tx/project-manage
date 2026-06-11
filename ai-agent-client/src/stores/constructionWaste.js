import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'
import { createWaste, deleteWaste } from '@/api/constructionWaste'

export const useConstructionWasteStore = defineStore('constructionWaste', () => {
  const records = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchAll(params = {}) {
    loading.value = true
    error.value = null
    try {
      const res = await client.get('/construction-wastes', { params })
      records.value = res.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchById(id) {
    loading.value = true
    error.value = null
    try {
      const res = await client.get(`/construction-wastes/${id}`)
      return res.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function create(payload) {
    loading.value = true
    error.value = null
    try {
      const res = await createWaste(payload)
      return res.data
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
      await deleteWaste(id)
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return { records, loading, error, fetchAll, fetchById, create, remove }
})
