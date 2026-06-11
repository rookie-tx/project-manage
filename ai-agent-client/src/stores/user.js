import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useUserStore = defineStore('user', () => {
  const users = ref([])
  const loading = ref(false)

  async function fetchUsers() {
    if (users.value.length > 0) return
    loading.value = true
    try {
      const { data } = await client.get('/users/select')
      users.value = data
    } finally {
      loading.value = false
    }
  }

  return { users, loading, fetchUsers }
})
