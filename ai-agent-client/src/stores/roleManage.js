import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'

export const useRoleManageStore = defineStore('roleManage', () => {
  const roles = ref([])
  const permissions = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchRoles() {
    loading.value = true
    error.value = null
    try {
      const { data } = await client.get('/roles')
      roles.value = data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchPermissions() {
    const { data } = await client.get('/permissions')
    permissions.value = data
  }

  async function createRole(payload) {
    error.value = null
    try {
      await client.post('/roles', payload)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function updateRole(id, payload) {
    error.value = null
    try {
      await client.put(`/roles/${id}`, payload)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function deleteRole(id) {
    error.value = null
    try {
      await client.delete(`/roles/${id}`)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function fetchRolePermissions(roleId) {
    const { data } = await client.get(`/roles/${roleId}/permissions`)
    return data
  }

  return {
    roles, permissions, loading, error,
    fetchRoles, fetchPermissions, createRole, updateRole, deleteRole, fetchRolePermissions,
  }
})
