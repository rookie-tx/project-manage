<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Form, FormItem, Input, Button, Alert } from 'ant-design-vue'
import client from '@/api/client'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const error = ref(null)
const success = ref(false)
const submitting = ref(false)

const formState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value) => {
        if (value !== formState.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

async function handleSubmit() {
  error.value = null
  success.value = false
  try {
    await formRef.value.validate()
    submitting.value = true
    await client.put('/auth/password', {
      oldPassword: formState.oldPassword,
      newPassword: formState.newPassword,
    })
    success.value = true
    // 密码修改后强制重新登录
    setTimeout(async () => {
      await auth.logout()
      router.push('/login')
    }, 1500)
  } catch (e) {
    if (e.errorFields) return
    error.value = e.message || '修改密码失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="settings-page">
    <h2>设置</h2>

    <div class="section">
      <h3>修改密码</h3>
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom: 16px" />
      <Alert v-if="success" type="success" message="密码修改成功，即将跳转到登录页..." style="margin-bottom: 16px" />

      <Form ref="formRef" :model="formState" :rules="rules" layout="vertical" style="max-width: 400px">
        <FormItem label="旧密码" name="oldPassword">
          <Input.Password v-model:value="formState.oldPassword" placeholder="请输入旧密码" />
        </FormItem>
        <FormItem label="新密码" name="newPassword">
          <Input.Password v-model:value="formState.newPassword" placeholder="请输入新密码，至少 6 个字符" />
        </FormItem>
        <FormItem label="确认新密码" name="confirmPassword">
          <Input.Password v-model:value="formState.confirmPassword" placeholder="请再次输入新密码" />
        </FormItem>
        <FormItem>
          <Button type="primary" :loading="submitting" @click="handleSubmit">修改密码</Button>
        </FormItem>
      </Form>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.settings-page h2 {
  margin: 0 0 24px;
  font-size: 18px;
}

.section {
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.section h3 {
  margin: 0 0 16px;
  font-size: 16px;
}
</style>
