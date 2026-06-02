## Why

当前系统只有登录认证功能，缺少用户管理与权限控制能力。管理员无法创建、编辑、禁用用户，所有认证用户权限均等，无法区分管理员、项目经理、财务等不同角色。系统需要一个用户管理模块来支撑多角色协作场景。

## What Changes

- 新建 **user 模块**（后端 DDD 四层），提供用户 CRUD 接口（分页列表、详情、创建、编辑、软删除）
- 新建 **RBAC 数据模型**：`t_role`、`t_permission`、`t_user_role`、`t_role_permission` 四张表及对应领域实体
- 扩展 **LoginContext**，在认证拦截器中加载用户角色和权限列表
- 在 **login 模块** 新增 `PUT /api/auth/password` 自服务修改密码接口
- `t_user` 表新增 `is_deleted` 字段，用户管理采用软删除策略
- 新增前端 **用户管理页面**（列表/创建/编辑），仅管理员角色可见
- 新增前端 **设置页面**，包含修改密码功能，激活当前闲置的"设置"菜单路由
- 新增独立菜单项 **"用户管理"**，仅管理员可见
- 预置种子数据：admin、project_manager、finance、user 四个角色及对应权限
- **BREAKING**: `GET /api/users` 从 login 模块迁移至 user 模块，增加分页和筛选参数，响应字段从 `{id, username}` 扩展为 `{id, username, status, roles, createdAt}`

## Capabilities

### New Capabilities
- `user-management-api`: 用户管理 CRUD 接口（分页列表、详情、创建、编辑、删除），含角色分配
- `user-management-ui`: 前端用户管理页面（列表/创建/编辑），管理员可见菜单和路由
- `rbac-model`: RBAC 数据模型（角色、权限、用户-角色关联、角色-权限关联），预置种子数据
- `password-change`: 自服务密码修改接口和前端设置页面
- `role-based-menu`: 基于角色的前端菜单控制，管理员可见"用户管理"菜单

### Modified Capabilities
- `user-list-api`: `GET /api/users` 从 login 模块迁移至 user 模块，增加分页参数（page/size）和筛选参数（keyword/status），响应字段扩展包含 roles 和 createdAt

## Impact

| 影响范围 | 说明 |
|----------|------|
| `ai-agent-server/` login 模块 | 新增 `PUT /api/auth/password`；LoginContext 扩展 roles/permissions；UserController 移除，改由 user 模块提供 `/api/users` |
| `ai-agent-server/` user 模块（新建） | 完整的 DDD 四层：api/application/domain/infrastructure |
| 数据库 | 新增 `t_role`、`t_permission`、`t_user_role`、`t_role_permission` 四张表；`t_user` 新增 `is_deleted` 字段 |
| `ai-agent-client/` router | 新增 `/users`、`/users/new`、`/users/:id`、`/settings` 路由 |
| `ai-agent-client/` menu | DashboardLayout 新增"用户管理"菜单项（管理员可见）；激活"设置"路由 |
| `ai-agent-client/` stores | 新增 `useUserManageStore`；扩展 `useAuthStore` 增加 permissions 字段 |
| `AuthenticationInterceptor` | 扩展加载 roles 和 permissions 到 LoginContext |
| `ErrorCode` | 新增 7xxx 范围用户管理错误码 |
