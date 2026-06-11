<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Form, FormItem, Input, InputPassword, Button, Alert } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const formRef = ref(null)
const formState = reactive({ username: '', password: '' })
const submitting = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function handleFinish() {
  submitting.value = true
  try {
    await auth.login(formState.username, formState.password)
    router.push('/dashboard')
  } catch {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-content">
        <div class="brand-icon">
          <span class="brand-emoji">🏗️</span>
        </div>
        <h1 class="brand-title">智能管理平台</h1>
        <p class="brand-desc">建筑项目管理 · 移动办公</p>
        <div class="brand-features">
          <div class="feat-item">
            <span class="feat-dot"></span>
            <span>项目全生命周期管理</span>
          </div>
          <div class="feat-item">
            <span class="feat-dot"></span>
            <span>采购申请与审批流程</span>
          </div>
          <div class="feat-item">
            <span class="feat-dot"></span>
            <span>工地考勤与废渣管理</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="form-wrapper">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-sub">请输入您的账号信息</p>
        <Alert
          v-if="auth.error"
          :message="auth.error"
          type="error"
          show-icon
          closable
          class="login-error"
          @close="auth.error = null"
        />
        <Form ref="formRef" :model="formState" :rules="rules" @finish="handleFinish">
          <FormItem name="username">
            <Input
              v-model:value="formState.username"
              size="large"
              placeholder="用户名"
              :disabled="submitting"
            >
              <template #prefix><UserOutlined /></template>
            </Input>
          </FormItem>
          <FormItem name="password">
            <InputPassword
              v-model:value="formState.password"
              size="large"
              placeholder="密码"
              :disabled="submitting"
            >
              <template #prefix><LockOutlined /></template>
            </InputPassword>
          </FormItem>
          <FormItem>
            <Button type="primary" html-type="submit" size="large" block :loading="submitting">
              登 录
            </Button>
          </FormItem>
        </Form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 0 0 44%;
  background: linear-gradient(160deg, #4f46e5 0%, #6366f1 40%, #8b5cf6 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  width: 400px; height: 400px;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
  top: -120px; right: -100px;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 240px; height: 240px;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
  bottom: -60px; left: -60px;
}

.left-content {
  text-align: center;
  color: #fff;
  position: relative;
  z-index: 1;
}

.brand-icon {
  width: 96px; height: 96px;
  background: rgba(255,255,255,0.15);
  border-radius: 24px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 28px;
  backdrop-filter: blur(8px);
}

.brand-emoji { font-size: 42px; }

.brand-title {
  font-size: 32px; font-weight: 800;
  margin: 0 0 10px; color: #fff;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 16px; opacity: 0.7;
  margin: 0 0 48px;
}

.brand-features {
  display: flex; flex-direction: column;
  gap: 14px; text-align: left;
  padding: 0 20px;
}

.feat-item {
  display: flex; align-items: center; gap: 12px;
  font-size: 15px; opacity: 0.8;
}

.feat-dot {
  width: 8px; height: 8px;
  border-radius: 50%; background: rgba(255,255,255,0.5);
  flex-shrink: 0;
}

.login-right {
  flex: 1;
  display: flex; justify-content: center; align-items: center;
  background: #fafbfc;
}

.form-wrapper {
  width: 400px; max-width: 88%;
}

.form-title {
  font-size: 26px; font-weight: 700;
  color: #1f2937; margin: 0 0 6px;
}

.form-sub {
  color: #9ca3af; margin: 0 0 32px;
  font-size: 15px;
}

.login-error { margin-bottom: 20px; }
</style>
