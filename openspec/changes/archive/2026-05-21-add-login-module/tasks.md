## 1. 依赖安装

- [x] 1.1 安装 vue-router、pinia、axios

## 2. API 层

- [x] 2.1 创建 `src/api/client.js` — axios 实例，baseURL: '/api'，withCredentials: true

## 3. 状态管理

- [x] 3.1 创建 `src/stores/auth.js` — Pinia auth store（user, loading, error, isAuth, fetchUser, login, logout）

## 4. 路由配置

- [x] 4.1 创建 `src/router/index.js` — 路由表（/ 重定向 /dashboard，/login，/dashboard）和 beforeEach 守卫

## 5. 页面组件

- [x] 5.1 创建 `src/views/LoginPage.vue` — ant-design-vue 登录表单，含 loading 和错误提示
- [x] 5.2 创建 `src/views/DashboardPage.vue` — 登录后的首页占位

## 6. 应用入口改造

- [x] 6.1 修改 `src/main.js` — 注册 router 和 pinia
- [x] 6.2 修改 `src/App.vue` — 改为 `<router-view />`，onMounted 调用 fetchUser 恢复会话，loading 期间显示 spin
