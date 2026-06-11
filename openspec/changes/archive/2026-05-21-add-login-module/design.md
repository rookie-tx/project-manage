## Context

当前项目是 Vue 3 + Vite 单页应用，使用 ant-design-vue 组件库，无路由、无状态管理。需要添加基于 session cookie 的登录模块。后端已存在三个接口：`POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/user/me`。

前端开发服务器（localhost:5173）与后端不同源，需处理跨域 cookie 传输。

## Goals / Non-Goals

**Goals:**
- 用户可使用用户名密码登录（ant-design-vue 表单）
- 登录后浏览器持有 session cookie，后续请求自动携带
- 刷新页面后自动恢复会话（调用 `/api/user/me`）
- 未登录用户访问受保护页面时重定向到 `/login`
- 登录成功后重定向到 `/dashboard`

**Non-Goals:**
- 注册功能、OAuth、SSO
- 记住密码、token refresh
- 权限/角色管理
- 后端 CORS 配置（后端独立处理）
- 后端实现（仅前端）

## Decisions

### 1. 跨域 cookie 策略：CORS + withCredentials

前端通过 axios `withCredentials: true` 确保每次请求携带 cookie。后端需设置 `Access-Control-Allow-Credentials: true` 和具体的 `Access-Control-Allow-Origin`（不能是 `*`）。Vite proxy 作为可选的开发辅助，但不依赖它处理跨域。

**替代方案**: 使用 Vite proxy 代理 API 请求，前端同源感知无需 `withCredentials`。缺点是生产环境仍需 CORS 方案，proxy 掩盖了跨域问题。

### 2. 路由设计：login 嵌套在主应用内

`/login` 是独立路由但共享同一个 Vue app 实例，不是独立页面。`/` 重定向到 `/dashboard`。路由守卫在 `beforeEach` 中检查 Pinia store 中 `isAuth` 状态。

**替代方案**: login 页作为独立 HTML 页面（多页应用）。缺点是用户状态不共享，且 Vue 生态下不自然。

### 3. 会话恢复：App.vue onMounted 发起 fetchUser

应用启动时，App.vue 的 `onMounted` 调用 `authStore.fetchUser()`（GET /api/user/me）。在请求完成前显示 loading 状态，避免"闪白"问题。

路由守卫依赖 Pinia store 中的 `loading` 状态：
- `loading === true` → 不判断，等待
- `loading === false && isAuth === false` → 重定向 /login
- `loading === false && isAuth === true` → 放行

### 4. 401 处理：分场景

| 场景 | HTTP 状态码 | 含义 | 处理方式 |
|------|------------|------|---------|
| `/api/user/me` | 401 | 未登录或 session 过期 | **静默**，`user = null` |
| `/api/auth/login` | 401 | 用户名或密码错误 | **展示**错误提示给用户 |

axios 拦截器不做统一 401 处理，由各 store action 自行处理。

### 5. 状态管理：Pinia Composition API

使用 Pinia 的 Setup Store 语法（composition style），与 Vue 3 `<script setup>` 风格一致。Store 暴露：`user`, `loading`, `error`, `isAuth`(computed), `fetchUser()`, `login()`, `logout()`。

**替代方案**: 普通 reactive + provide/inject。放弃的原因是 Pinia 提供 devtools 集成，且后续功能扩展时不需要迁移。

### 6. 路由表结构

```javascript
const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    component: LoginPage,
    meta: { requiresAuth: false },
  },
  {
    path: '/dashboard',
    component: DashboardPage,
    meta: { requiresAuth: true },
  },
]
```

`meta.requiresAuth` 控制守卫行为，便于后续添加更多受保护页面。

## Risks / Trade-offs

- **session cookie SameSite 限制**: 后端如果 set-cookie 未设置 `SameSite=None; Secure`，跨域场景下浏览器可能拒绝 cookie。→ 需要后端配合设置 cookie 属性。如果开发阶段使用 Vite proxy，可缓解此问题。
- **刷新时 loading 闪烁**: 网络慢时 `fetchUser` 返回慢，用户会看到 loading 状态。→ 设置合理的超时时间，且 loading 状态不阻塞 `/login` 页面渲染（公开路由无需等待）。
- **axios 实例单例**: 全应用共用一个 axios 实例，方便添加拦截器但也意味着一处配置影响全局。→ 当前规模下无风险，后续需要多实例时可封装工厂函数。