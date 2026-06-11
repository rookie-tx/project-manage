import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import client from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(true)
  const error = ref(null)
  const permissions = ref([])

  const isAuth = computed(() => user.value !== null)

  function hasPermission(perm) {
    return permissions.value.includes(perm)
  }

  async function fetchUser() {
    loading.value = true
    try {
      const { data } = await client.get('/auth/me')
      user.value = data
      permissions.value = data.permissions || []
    } catch {
      user.value = null
      permissions.value = []
    } finally {
      loading.value = false
    }
  }

  async function login(username, password) {
    error.value = null
    try {
      await client.post('/auth/login', { username, password })
      await fetchUser()
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function logout() {
    try {
      await client.post('/auth/logout')
    } finally {
      user.value = null
      permissions.value = []
    }
  }

  return { user, loading, error, permissions, isAuth, fetchUser, login, logout, hasPermission }
})
