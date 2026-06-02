## 1. 数据库变更

- [x] 1.1 在 `schema.sql` 中新增 `t_role`、`t_permission`、`t_user_role`、`t_role_permission` 四张表，含索引和外键
- [x] 1.2 在 `t_user` 表新增 `is_deleted SMALLINT DEFAULT 0` 字段
- [x] 1.3 插入预置种子数据：4 个角色、15 条权限、角色-权限关联

## 2. RBAC 领域层（user 模块 domain）

- [x] 2.1 创建 `UserStatus` 枚举（复用 login 模块现有枚举）
- [x] 2.2 创建 `Role` 领域实体（id, code, name, description），静态工厂方法
- [x] 2.3 创建 `Permission` 领域实体（id, code, name, description），静态工厂方法
- [x] 2.4 创建 `User` 领域实体（扩展版：id, username, passwordHash, status, isDeleted, createdAt, roles 集合），静态工厂方法
- [x] 2.5 创建 `UserRepository` 接口（findById, findByUsername, findAll 分页, save, update, softDelete）
- [x] 2.6 创建 `RoleRepository` 接口（findAll, findById, findByUserId）
- [x] 2.7 创建 `PermissionRepository` 接口（findAll, findByRoleIds）
- [x] 2.8 创建 `UserDomainService` 领域服务（createUser 含用户名唯一性检查, updateUser, softDeleteUser）

## 3. RBAC 基础设施层（user 模块 infrastructure）

- [x] 3.1 创建 `UserPO` 持久化对象
- [x] 3.2 创建 `RolePO` 持久化对象
- [x] 3.3 创建 `PermissionPO` 持久化对象
- [x] 3.4 创建 `UserManageMapper` MyBatis 接口及 XML（insert, selectById, selectByUsername, selectAll 分页+筛选, update, softDelete）
- [x] 3.5 创建 `RoleMapper` MyBatis 接口及 XML（selectAll, selectById, selectByUserId）
- [x] 3.6 创建 `PermissionMapper` MyBatis 接口及 XML（selectAll, selectByRoleIds）
- [x] 3.7 创建 `UserPoMapper` MapStruct 接口（PO ↔ Domain 转换）
- [x] 3.8 创建 `RolePoMapper` MapStruct 接口
- [x] 3.9 创建 `PermissionPoMapper` MapStruct 接口
- [x] 3.10 创建 `UserRepositoryImpl`（实现 UserRepository）
- [x] 3.11 创建 `RoleRepositoryImpl`（实现 RoleRepository）
- [x] 3.12 创建 `PermissionRepositoryImpl`（实现 PermissionRepository）
- [x] 3.13 创建 `UserDomainConfig`（暴露 UserDomainService 为 Bean）

## 4. 用户管理应用层（user 模块 application）

- [x] 4.1 创建 `UserDtoMapper` MapStruct 接口（Domain ↔ Response DTO，角色集合转角色名称列表）
- [x] 4.2 创建 `UserApplicationService`（编排 CRUD 操作，含事务管理）

## 5. 用户管理 API 层（user 模块 api）

- [x] 5.1 创建 `UserResponse` DTO（id, username, status, roles, createdAt）
- [x] 5.2 创建 `CreateUserRequest` DTO（@NotBlank username, @Size password, roleIds 可选）
- [x] 5.3 创建 `UpdateUserRequest` DTO（status 可选, roleIds 可选）
- [x] 5.4 创建 `UserController`（GET /api/users 分页, GET /api/users/{id}, POST /api/users, PUT /api/users/{id}, DELETE /api/users/{id}）
- [x] 5.5 在 `ErrorCode` 枚举中新增 7xxx 错误码（USER_NOT_FOUND, USERNAME_DUPLICATE, USER_DELETED）

## 6. LoginContext 扩展

- [x] 6.1 扩展 `LoginContext` record，新增 `roles` 和 `permissions` 字段
- [x] 6.2 在 `AuthenticationInterceptor` 中查询用户角色和权限并填充到 LoginContext
- [x] 6.3 更新 `LoginContextHolder` 和所有引用 `LoginContext` 构造的代码

## 7. 自服务改密

- [x] 7.1 创建 `ChangePasswordRequest` DTO（@NotBlank oldPassword, @NotBlank newPassword @Size(min=6)）
- [x] 7.2 在 `LoginDomainService` 新增 `changePassword(userId, oldPassword, newPassword)` 方法
- [x] 7.3 在 `LoginApplicationService` 新增 `changePassword()` 编排方法
- [x] 7.4 在 `SessionController` 新增 `PUT /api/auth/password` 端点
- [x] 7.5 在 `ErrorCode` 新增 `PASSWORD_SAME_AS_OLD(7004)` 和 `OLD_PASSWORD_INCORRECT(7005)`

## 8. 移除 login 模块中的 UserController

- [x] 8.1 删除 `login/api/UserController.java`（其功能由 user 模块的 UserController 替代）
- [x] 8.2 检查并更新所有依赖旧 `GET /api/users` 的前端调用（UserSelect 组件、useUserStore）

## 9. 前端 — Auth Store 扩展

- [x] 9.1 扩展 `useAuthStore`，新增 `permissions` 状态和 `hasPermission(perm)` 方法
- [x] 9.2 在 `fetchUser()` 中解析并存储用户权限列表
- [x] 9.3 在 `logout()` 中清空 permissions

## 10. 前端 — 用户管理页面

- [x] 10.1 创建 `useUserManageStore` Pinia store（用户列表、分页、搜索、CRUD 操作）
- [x] 10.2 创建 `src/views/user/UserListPage.vue`（表格、分页器、搜索框、状态筛选、操作按钮）
- [x] 10.3 创建 `src/views/user/UserCreatePage.vue`（用户名、密码、角色选择表单）
- [x] 10.4 创建 `src/views/user/UserEditPage.vue`（状态切换、角色重分配表单）
- [x] 10.5 在 `router/index.js` 中添加 `/users`、`/users/new`、`/users/:id/edit` 路由（meta: { requiresAuth: true, permission: 'user:read' }）
- [x] 10.6 在路由守卫中实现权限校验（无权限重定向）

## 11. 前端 — 设置页面

- [x] 11.1 创建 `src/views/settings/SettingsPage.vue`（修改密码表单：旧密码、新密码、确认新密码）
- [x] 11.2 在 `router/index.js` 中添加 `/settings` 路由（meta: { requiresAuth: true }）
- [x] 11.3 更新 `DashboardLayout.vue`，"设置"菜单项点击时跳转到 `/settings`

## 12. 前端 — 菜单与导航

- [x] 12.1 在 `DashboardLayout.vue` 中新增"用户管理"菜单项（key=users），使用 `v-if="auth.hasPermission('user:read')"` 控制显隐
- [x] 12.2 在 `handleMenuClick` 中处理 `users` key，跳转到 `/users`
- [x] 12.3 更新 `selectedKeys` watch，处理 `/users` 路径前缀

## 13. 前端 — UserSelect 适配

- [x] 13.1 更新 `api/client.js` 中 UserSelect 调用的 API 路径（无需改动——useUserStore 改为调用 `/users/select`）
- [x] 13.2 更新 `useUserStore` 适配新的分页响应格式或使用角色列表端点

## 14. 集成与验证

- [x] 14.1 编写 `UserDomainService` 单元测试（用户名唯一性、软删除逻辑）
- [x] 14.2 编写 `LoginDomainService` 单元测试（改密逻辑：成功、旧密码错误、密码相同）
- [ ] 14.3 启动后端服务，验证用户 CRUD 和改密端点（需数据库和运行环境）
- [ ] 14.4 启动前端，验证用户管理页面和设置页面（需前后端联调）
- [ ] 14.5 验证管理员角色可见"用户管理"菜单，普通用户不可见（需前后端联调）
