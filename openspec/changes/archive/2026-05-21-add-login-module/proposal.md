## Why

项目目前没有任何用户认证机制，所有页面公开访问。需要添加基于 session 的登录模块来控制页面访问权限，确保只有已登录用户才能访问受保护的页面。

## What Changes

- 新增 **vue-router** 路由，包含公开路由 `/login` 和受保护的 `/dashboard`
- 新增 **Pinia** auth store，管理用户登录状态、登录/登出/会话恢复
- 新增 **axios** HTTP 客户端，配置 `withCredentials` 携带 session cookie
- 新增 **LoginPage** 登录页面，使用 ant-design-vue 表单组件
- 新增 **DashboardPage** 占位页面，作为登录后的默认首页
- 新增 **路由守卫** `beforeEach`，未登录用户访问受保护页面时重定向到 `/login`
- App.vue 改为 `<router-view />`，启动时调用 `/api/user/me` 恢复会话

## Capabilities

### New Capabilities

- `auth`: 用户认证能力，包括 session 登录、登出、会话恢复，以及路由级别的登录鉴权

### Modified Capabilities

_无_

## Impact

- **新增依赖**: `vue-router`、`pinia`、`axios`
- **改动文件**:
  - `src/main.js` — 注册 router + pinia
  - `src/App.vue` — 改为 `<router-view />`，onMounted 时恢复会话
- **新增文件**:
  - `src/router/index.js` — 路由表 + beforeEach 守卫
  - `src/stores/auth.js` — Pinia auth store
  - `src/api/client.js` — axios 实例
  - `src/views/LoginPage.vue` — 登录页面
  - `src/views/DashboardPage.vue` — 首页占位
- **后端接口依赖**: `POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/user/me`
