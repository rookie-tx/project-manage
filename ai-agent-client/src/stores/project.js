import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

const PROJECT_TYPES = {
  0: '房建',
  1: '市政',
  2: '装修',
  3: '路桥',
  4: '水利',
  9: '其他',
}

const PROJECT_STATUSES = {
  0: '规划中',
  1: '在建',
  2: '暂停',
  3: '竣工',
  4: '已结算',
}

const STATUS_TRANSITIONS = {
  0: [1, 2, 3],
  1: [2, 3],
  2: [1, 3],
  3: [4],
  4: [],
}

export const useProjectStore = defineStore('project', () => {
  const items = ref([])
  const total = ref(0)
  const current = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function fetchList(params = {}) {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/projects', { params })
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
      const { data } = await client.get(`/projects/${id}`)
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
      const { data } = await client.post('/projects', formData)
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
      const { data } = await client.put(`/projects/${id}`, formData)
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
      await client.delete(`/projects/${id}`)
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  function getTypeLabel(code) {
    return PROJECT_TYPES[code] || '未知'
  }

  function getStatusLabel(code) {
    return PROJECT_STATUSES[code] || '未知'
  }

  function getAvailableStatuses(currentCode) {
    return STATUS_TRANSITIONS[currentCode] || []
  }

  function getTypeOptions() {
    return Object.entries(PROJECT_TYPES).map(([value, label]) => ({
      value: Number(value),
      label,
    }))
  }

  function getStatusOptions() {
    return Object.entries(PROJECT_STATUSES).map(([value, label]) => ({
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
    getTypeLabel,
    getStatusLabel,
    getAvailableStatuses,
    getTypeOptions,
    getStatusOptions,
  }
})
