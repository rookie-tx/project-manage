import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import client from '@/api/client'

export const useUserManageStore = defineStore('userManage', () => {
  const records = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)
  const searchParams = reactive({
    keyword: '',
    status: undefined,
    page: 1,
    size: 10,
  })

  async function fetchUsers() {
    loading.value = true
    error.value = null
    try {
      const params = {
        page: searchParams.page,
        size: searchParams.size,
      }
      if (searchParams.keyword) params.keyword = searchParams.keyword
      if (searchParams.status !== undefined && searchParams.status !== null) params.status = searchParams.status
      const { data } = await client.get('/users', { params })
      records.value = data.records
      total.value = data.total
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function createUser(payload) {
    error.value = null
    try {
      await client.post('/users', payload)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function updateUser(id, payload) {
    error.value = null
    try {
      await client.put(`/users/${id}`, payload)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function deleteUser(id) {
    error.value = null
    try {
      await client.delete(`/users/${id}`)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  function setPage(page) {
    searchParams.page = page
    return fetchUsers()
  }

  function setSize(size) {
    searchParams.size = size
    searchParams.page = 1
    return fetchUsers()
  }

  function search(keyword) {
    searchParams.keyword = keyword
    searchParams.page = 1
    return fetchUsers()
  }

  function filterByStatus(status) {
    searchParams.status = status
    searchParams.page = 1
    return fetchUsers()
  }

  return {
    records, total, loading, error, searchParams,
    fetchUsers, createUser, updateUser, deleteUser,
    setPage, setSize, search, filterByStatus,
  }
})
