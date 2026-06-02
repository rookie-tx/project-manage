## Context

当前系统只有 `login` 模块提供基础的认证与会话管理能力，`t_user` 表仅存储 `id/username/passwordHash/status` 四个业务字段。所有已认证用户权限均等，无法区分管理员、项目经理、财务等角色。前端"设置"菜单闲置无路由，无用户管理相关页面。

约束：
- 后端严格遵循 DDD 四层架构（api → application → domain ← infrastructure），domain 层零框架注解
- 前端使用 Vue 3 `<script setup>` + Pinia Setup Store + Ant Design Vue
- 统一响应格式 `Result<T>`，错误码分段管理
- P0 范围仅包含用户管理核心、RBAC 数据模型、自服务改密，不包含角色管理 UI 和 `@RequirePermission` 注解

## Goals / Non-Goals

**Goals:**
- 提供完整的用户 CRUD 接口（分页列表、详情、创建、编辑、软删除）
- 建立 RBAC 数据模型（角色、权限、用户-角色、角色-权限），预置四种角色和对应权限
- 认证拦截器加载用户角色和权限到 LoginContext
- 自服务修改密码（旧密码验证 + 新密码哈希）
- 前端用户管理页面（列表/创建/编辑），仅管理员可见
- 前端设置页面（修改密码）
- 软删除用户策略

**Non-Goals:**
- 角色管理 API 和 UI（创建/编辑/删除角色，给角色分配权限）— P1
- `@RequirePermission` 方法级权限注解 — P1
- 项目/采购模块接入权限控制 — P2
- 用户个人资料扩展字段（真实姓名、邮箱、手机号等）— 后续按需添加
- 密码重置（管理员重置他人密码）— 后续

## Decisions

### 决策 1: 新建 `user` 模块 vs 扩展 `login` 模块

**选择**: 新建独立的 `user` 模块

**理由**:
- `login` 模块职责单一（认证 + 会话），合并会导致模块臃肿
- `login/domain/User` 是最小认证视图，新增 RBAC 实体会污染认证域
- 符合 DDD 限界上下文原则 —— 用户管理和认证是两个不同的上下文
- P2 计划中项目/采购模块接入权限时，user 模块作为独立的权限来源更清晰

**替代方案**: 扩展 login 模块，将用户管理功能全部放入 login。放弃原因是职责混淆，且现有 `GET /api/users` 已在 login 模块中显得格格不入。

### 决策 2: RBAC 数据模型设计

**选择**: 四表模型 —— `t_role`、`t_permission`、`t_user_role`、`t_role_permission`

```
t_user ──MM──▶ t_user_role ◀──MM── t_role ──MM──▶ t_role_permission ◀──MM── t_permission
```

**理由**:
- 用户与角色多对多：一个用户可有多个角色（如同时是项目经理和财务）
- 角色与权限多对多：灵活分配权限组合
- 权限 code 采用 `资源:操作` 命名（如 `user:create`），便于前端按钮级控制

**权限 code 清单（预置）**:

| 资源 | read | create | update | delete | 特殊 |
|------|------|--------|--------|--------|------|
| user | ✅ | ✅ | ✅ | ✅ | `user:role:assign` |
| role | ✅ | — | — | — | — |
| project | ✅ | — | — | — | — |
| pr | ✅ | — | — | — | — |
| task | ✅ | — | — | — | — |

P1 阶段再扩展为完整 CRUD 权限。

**角色-权限映射（预置）**:

| 角色 | code | 权限 |
|------|------|------|
| 管理员 | admin | 全部权限 |
| 项目经理 | project_manager | user:read, project:*, pr:read, task:read |
| 财务 | finance | user:read, project:read, pr:*, task:* |
| 普通用户 | user | user:read(自身), project:read, pr:read, task:read |

### 决策 3: LoginContext 扩展方式

**选择**: 在 `LoginContext` record 中增加 `roles` 和 `permissions` 字段，认证拦截器查询后填充

```java
// 变更前
public record LoginContext(long userId, String username) {}

// 变更后
public record LoginContext(long userId, String username, List<String> roles, List<String> permissions) {}
```

**理由**:
- 认证拦截器每次请求都会执行，在 ThreadLocal 中携带角色和权限，后续 P1 做 `@RequirePermission` 时无需额外查询
- record 是不可变类型，线程安全

**数据来源**: 拦截器中通过 `UserRepository` → `t_user_role` → `t_role` → `t_role_permission` → `t_permission` 链路查询，组装为 `List<String>` 存放 code。

**性能考量**: 每次请求都查 4 张表关联。当前项目规模下可接受。后续若性能瓶颈出现，可引入缓存（如 Redis 缓存用户角色权限）。

### 决策 4: 软删除策略

**选择**: `t_user` 表新增 `is_deleted SMALLINT DEFAULT 0`，与现有 `t_project` 模式一致

**理由**:
- `t_user` 被其他表（`t_project.project_manager_id`、`t_user_role` 等）外键引用，物理删除会导致数据完整性问题
- 与项目/采购模块的软删除模式保持一致
- `status = DISABLED` 仅控制登录能力，`is_deleted` 表示逻辑删除，语义不同

**查询行为**: 所有 user 模块查询自动过滤 `is_deleted = 1` 的记录

### 决策 5: 用户创建时的角色分配

**选择**: 创建用户时可选分配角色。若不分配，用户仅能登录但无任何权限

**理由**:
- 校验逻辑简单：`auth.permissions` 为空 → 前端不显示任何受控内容
- 后续可扩展"新用户默认角色"配置

### 决策 6: 自服务改密端点位置

**选择**: 放在 login 模块，`PUT /api/auth/password`

**理由**:
- 密码是认证凭证，属于认证上下文
- login 模块已有 `PasswordHasher` 接口和 `BCryptPasswordHasher` 实现
- 避免 user 模块反向依赖 login 模块的密码哈希能力

### 决策 7: 前端用户管理页面架构

**选择**: 用户管理作为独立路由 `/users`，在 DashboardLayout 中新增独立菜单项

**路由**:
```
/users          → UserListPage.vue      (列表 + 搜索 + 分页)
/users/new      → UserCreatePage.vue    (创建表单 + 角色选择)
/users/:id      → UserDetailPage.vue    (详情 + 角色展示)
/users/:id/edit → UserEditPage.vue      (编辑表单 + 角色重分配)
```

**菜单可见性**: 使用 `v-if="auth.hasPermission('user:read')"` 控制菜单项显隐

**Store**: 新建 `useUserManageStore`，管理用户列表、分页、搜索状态

### 决策 8: ErrorCode 分配

**选择**: 用户管理使用 7xxx 范围

| 错误码 | 枚举名 | 说明 |
|--------|--------|------|
| 7001 | USER_NOT_FOUND | 用户不存在 |
| 7002 | USERNAME_DUPLICATE | 用户名重复 |
| 7003 | USER_DELETED | 用户已删除 |
| 7004 | PASSWORD_SAME_AS_OLD | 新密码与旧密码相同 |
| 7005 | OLD_PASSWORD_INCORRECT | 旧密码不正确 |

### 决策 9: 种子数据策略

**选择**: 使用 SQL 迁移脚本插入预置角色和权限，在 `schema.sql` 中追加

**预置数据**:
- 4 个角色 + 约 15 条权限 + 角色-权限关联
- 不预置用户-角色关联（现有 admin 用户由手动 SQL 或后续管理员操作赋权）
- 权限 code 使用英文，角色 name 使用中文

## Risks / Trade-offs

- **[风险] 认证拦截器中每次请求都查 4 表关联** → 当前用户量小可接受，后续通过缓存（Caffeine/Redis）优化
- **[风险] 种子角色和权限被误删** → P1 角色管理 UI 中增加保护标记，预置角色不可删除
- **[风险] login 和 user 模块共享 `t_user` 表** → 通过各自 Repository 接口隔离访问范围（login 只查认证字段，user 查完整信息），避免跨模块直接依赖
- **[取舍] 不在 P0 引入 `@RequirePermission` 注解** → P0 阶段仅加载权限到 LoginContext，前端通过 `auth.permissions` 做显隐控制，后端接口暂不做权限校验。这意味着熟悉 API 的用户可能绕过前端限制。这对内部管理系统可接受。

## Open Questions

1. 现有的 `admin` 用户应该预置为管理员角色吗？（建议：发布后手动执行 SQL 赋权，或作为 schema.sql 的一部分自动分配）
2. `GET /api/users` 从 login 迁移至 user 模块后，`UserSelect` 组件中使用的简单用户列表是否需要保留一个轻量端点？（建议：保留，但改为调用 user 模块的 `/api/users?size=all` 或新增 `/api/users/select` 专用端点）
