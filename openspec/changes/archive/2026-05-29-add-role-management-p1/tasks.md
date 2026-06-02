## 1. 数据库变更

- [x] 1.1 `t_role` 表新增 `is_protected SMALLINT NOT NULL DEFAULT 0` 字段
- [x] 1.2 更新种子数据：将 admin 角色的 `is_protected` 设为 1
- [x] 1.3 种子数据新增 4 条权限：`role:create`、`role:update`、`role:delete`、`role:permission:assign`，admin/普通用户角色关联更新

## 2. Role 实体扩展

- [x] 2.1 `Role.java` 新增 `isProtected` 字段 + `isProtected()` 方法
- [x] 2.2 `RolePO.java` 新增 `isProtected` 字段
- [x] 2.3 `RolePoMapper.java` `toDomain` 传入新字段
- [x] 2.4 `RoleMapper.java` 新增 `insert`、`update`、`deleteById` 方法
- [x] 2.5 `RoleMapper.xml` 新增对应 SQL + `selectAll` 查询加入 `is_protected`

## 3. Role 领域服务

- [x] 3.1 新建 `RoleRepository` 接口新增 `save`、`update`、`delete` 方法
- [x] 3.2 新建 `RoleRepositoryImpl` 实现新增方法
- [x] 3.3 新建 `RoleDomainService`（create/update/delete：检查 admin 保护 + 名称唯一性）
- [x] 3.4 新建 `RoleDomainConfig` 注册 Bean

## 4. Permission 扩展

- [x] 4.1 `PermissionMapper.java` 新增 `selectByRoleId` 方法
- [x] 4.2 `PermissionMapper.xml` 新增对应 SQL
- [x] 4.3 `PermissionRepository` 接口新增 `findByRoleId` 方法
- [x] 4.4 `PermissionRepositoryImpl` 实现新增方法

## 5. 角色管理应用层

- [x] 5.1 新建 `RoleApplicationService`（编排 CRUD + 权限分配）

## 6. 角色管理 API 层

- [x] 6.1 新建 `RoleResponse` DTO
- [x] 6.2 新建 `CreateRoleRequest` / `UpdateRoleRequest` DTO
- [x] 6.3 新建 `RoleController`（GET /api/roles, POST, PUT /api/roles/{id}, DELETE /api/roles/{id}）
- [x] 6.4 新建 `PermissionController`（GET /api/permissions, GET /api/roles/{id}/permissions）
- [x] 6.5 `ErrorCode` 新增 `ROLE_CODE_DUPLICATE(7006)` 和 `ROLE_PROTECTED(7007)`
- [x] 6.6 i18n 新增对应消息

## 7. @RequirePermission 注解

- [x] 7.1 新建 `cn.tx.aiagent.common.security` 包
- [x] 7.2 新建 `@RequirePermission` 注解
- [x] 7.3 新建 `PermissionAspect` AOP 切面（`@Around` Controller 方法，校验 LoginContextHolder 中的权限）

## 8. 已有 Controller 加权限保护

- [x] 8.1 `UserController` 方法添加 `@RequirePermission` 注解（create→user:create, update→user:update, delete→user:delete）
- [x] 8.2 `RoleController` 方法添加对应权限注解
- [x] 8.3 `WebMvcConfig` 注册 AOP 切面或启用 AspectJ 自动代理

## 9. 前端 — 角色管理页面

- [x] 9.1 新建 `useRoleManageStore` Pinia store（角色列表、权限列表、CRUD 操作）
- [x] 9.2 新建 `src/views/role/RoleListPage.vue`（表格 + 创建/编辑弹窗 + 权限复选框分组）
- [x] 9.3 在 `router/index.js` 添加 `/roles` 路由（meta: { requiresAuth: true, permission: 'role:read' }）
- [x] 9.4 `DashboardLayout.vue` 新增"角色管理"菜单项（key=roles），使用 `auth.hasPermission('role:read')` 控制，更新 selectedKeys/handleMenuClick
- [x] 9.5 权限复选框按资源分组显示（user:* / role:* / project:* / pr:* / task:*）

## 10. 集成与验证

- [ ] 10.1 编写 `RoleDomainService` 单元测试（创建、保护角色删除拦截）
- [ ] 10.2 编写 `PermissionAspect` 单元测试（权限通过、权限拒绝、未登录）
- [ ] 10.3 启动后端验证角色 CRUD 和权限分配端点
- [ ] 10.4 启动前端验证角色管理页面和权限保护
