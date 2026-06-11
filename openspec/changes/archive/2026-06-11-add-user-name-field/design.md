## Context

当前 `t_user` 表无姓名字段，系统以 `username`（手机号）作为唯一身份标识。Header 头像旁、临时工列表"姓名"列、审批历史"审批人"等展示位置均显示手机号而非真实姓名。

用户要求新增真实姓名字段，手机号仍作为登录账号（`username`），名字仅用于展示。

## Goals / Non-Goals

**Goals:**
- `t_user` 新增 `name` 字段，存储用户真实姓名
- `LoginContext` / JWT / `/api/auth/me` 携带 `name`，前端可获取
- 临时工创建时填写 `name`，写入 `t_user`
- 用户编辑页面可修改 `name`
- Header、临时工列表、审批历史等位置优先展示 `name`，null 时回退 `username`
- 已有数据 `name` 为 null，前端降级显示手机号

**Non-Goals:**
- 不改变登录机制（username 仍为手机号）
- 不修改临时工编辑接口（名字走用户编辑）
- 不强制所有用户必须设置名字（可为空）

## Decisions

### 决策 1: name 字段放 t_user 而非 t_temp_worker

**选择**: `t_user` 表新增 `name VARCHAR(50)`

**理由**: "姓名"是人的属性而非工种的属性；LoginContext 需携带 name 用于前端全局展示；用户列表等场景也需要展示姓名；非临时工用户后续也可能需要设置名字。

**替代方案**: 放 `t_temp_worker` — 拒绝，因为查询和会话都需要额外 JOIN。

### 决策 2: LoginContext 携带 name

**选择**: `LoginContext` record 新增 `name` 字段（可为 null）

**理由**: 前端 Header 通过 `/api/auth/me` 获取当前用户信息，需要 name 实时展示。JWT 令牌中也需要存储 name 以便无状态认证时可用。

**影响**: `JwtTokenProvider` 创建/解析令牌时读写 name；`AuthenticationInterceptor` 从 DB 查 user.name 写入；`LoginApplicationService` 和 `WechatAuthController` 构造 LoginContext 时传入 name。

### 决策 3: name 写入和更新解耦

**选择**: 临时工创建接口负责**写入** name；用户编辑接口负责**更新** name

**理由**: 临时工创建是批量录入场景，name 必填方便一次性录入；后续改名字走用户管理页面（已有 `PUT /api/users/:id`），避免临时工编辑接口臃肿。

### 决策 4: 审批流程 assigneeName 用 name

**选择**: `TaskApplicationService.completeTask()` 中 `assigneeName` 从 `LoginContextHolder.getUsername()` 改为 `LoginContextHolder.getName()`

**理由**: 审批历史展示"审批人：张三"而非"审批人：13800138000"。`name` 为 null 时（审批人是管理员等未设名字的用户），存 null，前端通过 `assigneeName || assignee` 降级展示 userId。

### 决策 5: 前端降级策略

**选择**: 所有展示位置统一使用 `name || username` 模式

**理由**: 已有用户 name 为 null，不能直接替换。新创建的临时工 name 必填所以始终有值，但管理员等"老用户"暂无名字时仍需展示可辨识的身份标识（手机号）。

## Risks / Trade-offs

- **name 可为 null 导致展示不一致**: 临时工有名字但管理员可能没有 → 统一降级策略 `name || username`，后续管理员可在用户设置中补填
- **JWT 令牌中 name 变更不实时**: JWT 有效期内 name 修改后不会立即生效 → token 过期（默认数天）后自然刷新；可通过退出重新登录立即生效
- **已有临时工无名字**: 系统上线后 old 数据 name=null → 列表显示手机号，管理员可逐个编辑补填名字

## Migration Plan

1. 执行 DDL `ALTER TABLE t_user ADD COLUMN IF NOT EXISTS name VARCHAR(50)` — 对已有数据无影响
2. 部署后端 → 新接口生效，旧客户端仍兼容（name 为 null）
3. 部署前端 → 新建临时工有名字；旧数据仍显示手机号
4. 可选：管理员通过用户编辑页面为已有临时工补填名字

## Open Questions

（无）
