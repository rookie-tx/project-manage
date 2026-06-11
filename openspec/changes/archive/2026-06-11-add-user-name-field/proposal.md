## Why

临时工当前只有手机号作为身份标识，系统各处（Header、临时工列表、审批历史）展示的都是手机号，缺少"人的名字"。需要在 `t_user` 表增加真实姓名字段，用于全系统展示，手机号仍作为登录账号。

## What Changes

- `t_user` 表新增 `name VARCHAR(50)` 字段，可为空
- `LoginContext`、JWT 令牌、`/api/auth/me` 响应均携带 `name`
- `POST /api/temp-workers` 创建请求新增 `name` 字段（必填），创建时写入 `t_user.name`
- `PUT /api/users/:id` 编辑请求新增 `name` 字段，通过用户编辑接口修改名字
- 临时工列表查询（`GET /api/projects/:id/temp-workers`）返回 `name` 字段（来自 `u.name` JOIN）
- 审批流程 `completeTask` 中 `assigneeName` 改用 `LoginContextHolder.getName()`，存储审批人真实姓名
- 前端 Header 头像旁展示 `name || username`，临时工列表"姓名"列展示 `name`
- 已有数据的 `name` 为 null，前端降级显示 `username`（手机号）

## Capabilities

### New Capabilities
- `user-name-field`: 用户新增真实姓名字段，贯穿持久层、领域层、认证链路、API 响应，前端展示使用 name 优先

### Modified Capabilities
<!-- 无现有活跃 spec 需要修改 -->
（无）

## Impact

- **数据库**: `t_user` 加 `name` 列（DDL 增量迁移）
- **后端 User 模块**: `UserPO`, `User`, `UserMapper.xml`, `UserDomainService`, `UserResponse`, `CreateUserRequest`
- **后端认证链路**: `LoginContext`, `LoginContextHolder`, `JwtTokenProvider`, `AuthenticationInterceptor`, `LoginApplicationService`, `WechatAuthController`, `LoginResponse`, `SessionController`
- **后端 TempWorker 模块**: `CreateTempWorkerRequest`, `TempWorkerDomainService`, `TempWorkerController`, `TempWorker`, `TempWorkerPO`, `TempWorkerMapper.xml`
- **后端审批流程**: `TaskApplicationService.completeTask()` — `assigneeName` 改用 `getName()`
- **前端**: `TempWorkerTab.vue`, `DashboardLayout.vue`, 用户管理页面
