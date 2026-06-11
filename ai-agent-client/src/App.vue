<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Spin, Modal } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'
import client from '@/api/client'

const router = useRouter()
const auth = useAuthStore()

const showPasswordModal = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdError = ref('')
const pwdSubmitting = ref(false)

const mustChangePwd = computed(() => auth.user?.mustChangePassword === true)

// 路由守卫：必须改密时锁死页面
router.beforeEach((to) => {
  if (auth.isAuth && auth.user?.mustChangePassword && to.path !== '/login') {
    showPasswordModal.value = true
    return false
  }
  return true
})

onMounted(async () => {
  await auth.fetchUser()
  if (!auth.isAuth) {
    router.replace('/login')
    return
  }
  if (mustChangePwd.value) {
    showPasswordModal.value = true
  }
})

async function handleChangePassword() {
  pwdError.value = ''
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    pwdError.value = '两次输入的密码不一致'
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    pwdError.value = '密码至少 6 个字符'
    return
  }
  pwdSubmitting.value = true
  try {
    await client.put('/auth/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    auth.user.mustChangePassword = false
    showPasswordModal.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    router.push('/attendance')
  } catch (e) {
    pwdError.value = e.message || '修改失败'
  } finally {
    pwdSubmitting.value = false
  }
}
</script>

<template>
  <div v-if="auth.loading" class="app-loading"><Spin size="large" /></div>
  <template v-else>
    <router-view />
    <Modal
      :open="showPasswordModal"
      title="首次登录，请修改密码"
      :closable="false"
      :mask-closable="false"
      :confirm-loading="pwdSubmitting"
      @ok="handleChangePassword"
      ok-text="确认修改"
      cancel-text=""
      :cancel-button-props="{ style: { display: 'none' } }"
    >
      <p style="margin-bottom:12px;color:#666">为了账户安全，首次登录必须修改密码</p>
      <div v-if="pwdError" style="color:#ff4d4f;margin-bottom:12px">{{ pwdError }}</div>
      <div style="display:flex;flex-direction:column;gap:12px">
        <a-input-password v-model:value="passwordForm.oldPassword" placeholder="旧密码（初始密码 123456）" />
        <a-input-password v-model:value="passwordForm.newPassword" placeholder="新密码（至少 6 个字符）" />
        <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="确认新密码" />
      </div>
    </Modal>
  </template>
</template>

<style>
.app-loading { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f3f4f6; }
</style>
