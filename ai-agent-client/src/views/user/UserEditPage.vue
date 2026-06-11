<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserManageStore } from '@/stores/userManage'
import { Form, FormItem, Input, Button, Select, Alert, Spin } from 'ant-design-vue'
import client from '@/api/client'

const router = useRouter()
const route = useRoute()
const store = useUserManageStore()

const formRef = ref(null)
const loading = ref(true)
const submitting = ref(false)
const error = ref(null)
const roles = ref([])

const formState = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  status: 1,
  roleIds: [],
})

const userId = Number(route.params.id)

onMounted(async () => {
  try {
    const [userRes, rolesRes] = await Promise.all([
      client.get(`/users/${userId}`),
      client.get('/roles'),
    ])
    const user = userRes.data
    const allRoles = rolesRes.data
    roles.value = allRoles

    formState.username = user.username
    formState.name = user.name || ''
    formState.email = user.email || ''
    formState.phone = user.phone || ''
    formState.status = user.status === 'ACTIVE' ? 1 : 0

    // 根据角色名称映射到角色 ID
    if (user.roles && user.roles.length > 0) {
      formState.roleIds = allRoles
        .filter(r => user.roles.includes(r.name))
        .map(r => r.id)
    } else {
      formState.roleIds = []
    }
  } catch (e) {
    error.value = e.message || '加载用户信息失败'
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  error.value = null
  try {
    await formRef.value.validate()
    submitting.value = true
    await store.updateUser(userId, {
      name: formState.name || undefined,
      email: formState.email || undefined,
      phone: formState.phone || undefined,
      status: formState.status,
      roleIds: formState.roleIds,
    })
    router.push('/users')
  } catch (e) {
    if (e.errorFields) return
    error.value = e.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/users')
}
</script>

<template>
  <div class="user-edit-page">
    <h2>编辑用户</h2>

    <Spin v-if="loading" size="large" />

    <template v-else>
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom: 16px" />

      <Form ref="formRef" :model="formState" layout="vertical" style="max-width: 480px">
        <FormItem label="用户名">
          <Input v-model:value="formState.username" disabled />
        </FormItem>
        <FormItem label="姓名">
          <Input v-model:value="formState.name" placeholder="请输入真实姓名" :maxlength="50" />
        </FormItem>
        <FormItem label="邮箱">
          <Input v-model:value="formState.email" placeholder="请输入邮箱" :maxlength="100" />
        </FormItem>
        <FormItem label="手机号">
          <Input v-model:value="formState.phone" placeholder="请输入手机号" :maxlength="20" />
        </FormItem>
        <FormItem label="状态" name="status">
          <Select v-model:value="formState.status" style="width: 200px">
            <Select.Option :value="1">正常</Select.Option>
            <Select.Option :value="0">禁用</Select.Option>
          </Select>
        </FormItem>
        <FormItem label="角色" name="roleIds">
          <Select
            v-model:value="formState.roleIds"
            mode="multiple"
            placeholder="请选择角色"
            :options="roles.map(r => ({ value: r.id, label: r.name }))"
          />
        </FormItem>
        <FormItem>
          <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
          <Button style="margin-left: 12px" @click="handleCancel">取消</Button>
        </FormItem>
      </Form>
    </template>
  </div>
</template>

<style scoped>
.user-edit-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.user-edit-page h2 {
  margin: 0 0 24px;
  font-size: 18px;
}
</style>
