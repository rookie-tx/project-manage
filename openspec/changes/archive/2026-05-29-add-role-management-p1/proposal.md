## Why

P0 已建立 RBAC 数据模型和用户管理功能，但角色的创建、编辑、删除和权限分配只能通过数据库操作完成。管理员需要可视化的角色管理界面来动态调整权限配置，同时后端需要方法级权限校验来保护 API 端点安全。P0 中权限仅加载到 LoginContext 供前端菜单控制，后端接口仍无保护。

## What Changes

- 新建 **角色管理 API**：角色的创建、编辑、删除，权限列表查询，角色-权限关联的查询与更新
- 新建 **`@RequirePermission` 注解**：标记在 Controller 方法上，通过 AOP 切面拦截并校验当前用户的权限
- 新建前端 **角色管理页面**：角色列表、创建/编辑表单、权限勾选树
- 前端新增独立菜单项 **"角色管理"**，需 `role:read` 权限可见
- `t_role` 表新增 `is_protected` 字段，保护 `admin` 角色不被删除
- `t_role_permission` 种子数据新增 `role:create`、`role:update`、`role:delete`、`role:permission:assign` 四条权限

## Capabilities

### New Capabilities
- `role-management-api`: 角色 CRUD 接口（列表、创建、编辑、删除），权限列表查询，角色-权限关联查询与更新；admin 角色删除保护
- `role-management-ui`: 前端角色管理页面（表格列表 + 创建/编辑弹窗 + 权限勾选树），独立菜单项
- `require-permission-annotation`: `@RequirePermission` 注解 + AOP 切面，校验 LoginContext 中的 permissions

### Modified Capabilities
<!-- None -->

## Impact

| 影响范围 | 说明 |
|----------|------|
| `ai-agent-server/` user 模块 | 新增 `RoleController`、`PermissionController`、`RoleApplicationService`、`RoleDomainService`；RoleMapper/PermissionMapper 扩展 insert/update/delete |
| 数据库 | `t_role` 新增 `is_protected SMALLINT DEFAULT 0`；种子数据新增 4 条权限 |
| `ai-agent-server/` common 模块 | 新增 `@RequirePermission` 注解 + `PermissionAspect` 切面 |
| `ai-agent-client/` router | 新增 `/roles`、`/roles/new`、`/roles/:id/edit` 路由 |
| `ai-agent-client/` menu | DashboardLayout 新增"角色管理"菜单项（需 `role:read` 权限） |
| `ai-agent-client/` views | 新建 `RoleListPage.vue`、`RoleEditPage.vue` |
| `ErrorCode` | 新增 7xxx 范围角色管理错误码 |
