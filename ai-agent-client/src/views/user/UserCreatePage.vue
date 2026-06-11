<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserManageStore } from '@/stores/userManage'
import { Form, FormItem, Input, Button, Select, Alert } from 'ant-design-vue'
import client from '@/api/client'

const router = useRouter()
const store = useUserManageStore()

const formRef = ref(null)
const formState = reactive({
  username: '',
  name: '',
  password: '',
  email: '',
  phone: '',
  roleIds: [],
})

const roles = ref([])
const submitting = ref(false)
const error = ref(null)

async function loadRoles() {
  try {
    const { data } = await client.get('/roles')
    roles.value = data
  } catch { /* ignore */ }
}

loadRoles()

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 个字符', trigger: 'blur' }],
}

async function handleSubmit() {
  error.value = null
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      username: formState.username,
      name: formState.name || undefined,
      password: formState.password,
      email: formState.email || undefined,
      phone: formState.phone || undefined,
    }
    if (formState.roleIds.length > 0) {
      payload.roleIds = formState.roleIds
    }
    await store.createUser(payload)
    router.push('/users')
  } catch (e) {
    if (e.errorFields) return
    error.value = e.message || '创建失败'
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.push('/users')
}
</script>

<template>
  <div class="user-create-page">
    <h2>创建用户</h2>

    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom: 16px" />

    <Form ref="formRef" :model="formState" :rules="rules" layout="vertical" style="max-width: 480px">
      <FormItem label="用户名" name="username">
        <Input v-model:value="formState.username" placeholder="请输入用户名" :maxlength="50" />
      </FormItem>
      <FormItem label="姓名" name="name">
        <Input v-model:value="formState.name" placeholder="请输入真实姓名（可选）" :maxlength="50" />
      </FormItem>
      <FormItem label="密码" name="password">
        <Input.Password v-model:value="formState.password" placeholder="请输入密码，至少 6 个字符" />
      </FormItem>
      <FormItem label="邮箱" name="email">
        <Input v-model:value="formState.email" placeholder="请输入邮箱（可选）" :maxlength="100" />
      </FormItem>
      <FormItem label="手机号" name="phone">
        <Input v-model:value="formState.phone" placeholder="请输入手机号（可选）" :maxlength="20" />
      </FormItem>
      <FormItem label="角色（可选）" name="roleIds">
        <Select
          v-model:value="formState.roleIds"
          mode="multiple"
          placeholder="请选择角色"
          :options="roles.map(r => ({ value: r.id, label: r.name }))"
        />
      </FormItem>
      <FormItem>
        <Button type="primary" :loading="submitting" @click="handleSubmit">创建</Button>
        <Button style="margin-left: 12px" @click="handleCancel">取消</Button>
      </FormItem>
    </Form>
  </div>
</template>

<style scoped>
.user-create-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.user-create-page h2 {
  margin: 0 0 24px;
  font-size: 18px;
}
</style>
