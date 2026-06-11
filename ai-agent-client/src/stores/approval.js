import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useApprovalStore = defineStore('approval', () => {
  const tasks = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)

  async function fetchTasks(params = {}) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/tasks', { params })
      tasks.value = data
      total.value = Array.isArray(data) ? data.length : 0
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function completeTask(taskId, approved, comment) {
    loading.value = true
    error.value = null
    try {
      await client.post(`/tasks/${taskId}/complete`, { approved, comment })
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchHistory(businessType, businessId) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/tasks/history', {
        params: { businessType, businessId }
      })
      return data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return { tasks, total, loading, error, fetchTasks, completeTask, fetchHistory }
})
