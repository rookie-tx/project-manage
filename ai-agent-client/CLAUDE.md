## 前端 — 目录结构

```
src/
├── api/client.js          # Axios 实例 + 拦截器
├── assets/                # 全局 CSS
│   ├── base.css           # CSS reset、颜色变量
│   └── main.css           # #app + 链接样式，导入 base.css
├── components/            # 可复用 UI 组件
├── router/index.js        # 路由 + beforeEach 守卫
├── stores/                # Pinia stores（一个文件一个领域）
├── views/                 # 页面级组件（一个路由一个文件）
├── App.vue                # 根组件：router-view + 会话恢复
└── main.js                # 入口：createApp 注册插件
```

- **views/** — 页面级组件，一个路由对应一个
- **components/** — 可复用/共享组件
- **stores/** — Pinia stores，一个文件一个业务领域
- **router/** — 单一 `index.js`
- **api/** — HTTP 客户端层

## 前端 — 组件规范

所有组件使用 `<script setup>` 语法，禁止 Options API。

```vue
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useXxxStore } from '@/stores/xxx'
import { Form, FormItem, Input, Button } from 'ant-design-vue'

const formState = reactive({ username: '', password: '' })
const loading = ref(false)

async function handleSubmit() { /* ... */ }
</script>

<template>
  <!-- Ant Design Vue 组件使用 a- 前缀 -->
</template>

<style scoped>
/* scoped 样式，类名以组件名作为前缀 kebab-case */
</style>
```

- 文件名：Vue 组件 **PascalCase**（`LoginPage.vue`），JS 模块 **camelCase**
- 变量/函数：**camelCase**（`formState`, `handleSubmit`）
- Store 命名：`useXxxStore`（`useAuthStore`）
- 路由名称：**PascalCase**（`'Login'`, `'Dashboard'`）

## 前端 — API Client 模式

位于 `src/api/client.js` 的单一 axios 实例。后端基址 `http://localhost:8080/api`。

```javascript
import client from '@/api/client'
const response = await client.post('/auth/login', { username, password })
// response.data 已解析为后端 data 字段内容
```

请求/响应处理规则：
- `code === 0` → 成功，自动解包 `data`
- `code !== 0` → 拒绝 `Error(msg)`
- `withCredentials: true`（携带 session cookie）
- 401 拦截器清除认证状态并重定向到 `/login`

## 前端 — Pinia Store 模式

仅使用 Setup Store（Composition API）风格，禁止 Options API stores。

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import client from '@/api/client'

export const useXxxStore = defineStore('xxx', () => {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isReady = computed(() => !!data.value && !loading.value)

  async function fetchData() {
    loading.value = true
    error.value = null
    try {
      const res = await client.get('/xxx')
      data.value = res.data
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, isReady, fetchData }
})
```

- State：基元类型用 `ref()`，对象/数组用 `ref(null)`
- Computed：`computed()`
- Actions：async 函数，try/catch/finally
- 错误模式：开始时清空 error，catch 中设置 `error.value = e.message`，重新抛出
- 返回所有 state/computed/actions 为单一对象字面量

## 前端 — Router 模式

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginPage.vue'),  // 懒加载
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardPage.vue'),
    meta: { requiresAuth: true },
  },
]

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (auth.loading) { /* 等待会话恢复 */ }
  if (to.meta.requiresAuth && !auth.isAuth) { return next('/login') }
  if (to.path === '/login' && auth.isAuth) { return next('/dashboard') }
  next()
})
```

- 所有路由使用 **懒加载** 通过动态 `import()`
- 需要认证的路由使用 `meta: { requiresAuth: true }`
- `beforeEach` 守卫处理：加载状态等待、认证重定向、已登录用户回退重定向

## 前端 — 错误处理（三层）

1. **API 拦截器**（`client.js`）：解包 `{ code, msg, data }` 信封；`code !== 0` 时 reject `Error(msg)`；401 时重定向
2. **Store**：捕获错误，将 `e.message` 存入 `error` ref
3. **组件**：通过 ant-design-vue 的 `<Alert>` 展示 `store.error`

用户可见的错误信息来自**后端**的 `msg` 字段。

## 前端 — 会话恢复模式 (App.vue)

```vue
<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Spin } from 'ant-design-vue'

const auth = useAuthStore()
onMounted(() => auth.fetchUser())
</script>

<template>
  <Spin v-if="auth.loading" size="large" />
  <router-view v-else />
</template>
```

## 前端 — Ant Design Vue 约定

- 全局注册于 `main.js`：`app.use(Antd)`
- 模板中使用 `a-` 前缀：`<a-layout>`, `<a-menu>`, `<a-button>`, `<a-form>`, `<a-form-item>`, `<a-input>`, `<a-alert>`, `<a-spin>`, `<a-dropdown>`, `<a-avatar>`
- 图标从 `@ant-design/icons-vue` 单独导入：`<UserOutlined />`, `<LockOutlined />`
- 表单验证使用 Ant Design 的 `rules` 属性

## 前端 — CSS 约定

- Scoped 样式：每个 `.vue` 组件使用 `<style scoped>`
- 类命名：组件名前缀，kebab-case（`.login-page`, `.login-form-wrapper`, `.dashboard-layout`, `.dashboard-header`）
- 全局变量在 `base.css`：颜色方案，通过 `prefers-color-scheme` 支持亮色/暗色模式

## 前端 — 应用启动 (main.js)

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Antd)
app.mount('#app')
```