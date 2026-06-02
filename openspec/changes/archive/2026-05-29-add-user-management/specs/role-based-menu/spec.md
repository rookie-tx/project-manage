## ADDED Requirements

### Requirement: 基于角色的菜单可见性
系统 SHALL 在前端导航菜单中根据当前用户的角色/权限控制菜单项的显示与隐藏。

#### Scenario: 管理员看到用户管理菜单
- **WHEN** 拥有 `user:read` 权限的用户登录
- **THEN** DashboardLayout 顶部导航显示"用户管理"菜单项

#### Scenario: 普通用户看不到用户管理菜单
- **WHEN** 没有 `user:read` 权限的用户登录
- **THEN** DashboardLayout 顶部导航不显示"用户管理"菜单项

#### Scenario: 所有人可见设置菜单
- **WHEN** 任意已登录用户访问主页面
- **THEN** 顶部导航始终显示"设置"菜单项

### Requirement: 用户管理路由守卫
系统 SHALL 在路由层面校验用户是否有权限访问用户管理相关页面。

#### Scenario: 管理员访问用户管理路由
- **WHEN** 拥有 `user:read` 权限的用户导航到 `/users`
- **THEN** 正常渲染用户列表页面

#### Scenario: 无权限用户访问用户管理路由
- **WHEN** 没有 `user:read` 权限的用户直接输入 URL 导航到 `/users`
- **THEN** 路由守卫重定向到 `/dashboard` 或显示无权限提示

### Requirement: Auth Store 扩展
系统 SHALL 在 `useAuthStore` 中扩展存储当前用户的权限列表，并提供 `hasPermission` 辅助方法。

#### Scenario: 登录后获取权限
- **WHEN** 用户登录成功并调用 `fetchUser()`
- **THEN** auth store 的 `permissions` 字段包含当前用户的权限 code 列表

#### Scenario: hasPermission 判断
- **WHEN** 调用 `auth.hasPermission('user:read')`
- **THEN** 若 permissions 数组包含 `user:read` 返回 true，否则返回 false

#### Scenario: 登出后清空权限
- **WHEN** 用户执行登出
- **THEN** auth store 的 `permissions` 重置为空数组
