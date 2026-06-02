## Why

项目施工现场有大量临时工人，需要管理其基本信息、工种和入离职时间。临时工需要通过手机号登录系统进行后续打卡操作。当前系统只有系统用户（admin/项目经理/财务等），缺少专门的临时工身份和生命周期管理。

## What Changes

- 新建 **tempworker 模块**（后端 DDD 四层），提供临时工 CRUD
- 创建临时工时：自动创建 `t_user` 记录（username=手机号，初始密码，角色=temp_worker），再创建 `t_temp_worker` 记录关联 user_id 和 project_id
- 数据库新增 `t_temp_worker` 表（user_id FK, project_id FK, worker_type, entry_date, exit_date, status）
- 种子数据新增 `temp_worker` 角色 + 最小权限（仅 `user:read` 查看自身，后续增加打卡权限）
- `t_user` 表新增 `must_change_password SMALLINT DEFAULT 0` 字段，临时工首次登录时标记为 1
- `GET /api/auth/me` 返回 `mustChangePassword` 字段，前端首页拦截强制跳转修改密码
- 前端项目详情页新增 **"临时工"** Tab，展示该项目下临时工列表，支持新增/编辑/离职

## Capabilities

### New Capabilities
- `temp-worker-crud`: 临时工 CRUD（创建时联动创建用户，离职时禁用用户，按项目筛选）
- `force-password-change`: 首次登录强制修改密码机制

### Modified Capabilities
- `password-change`: 修改密码成功后清除 `must_change_password` 标记
- `project-management-ui`: 项目详情页新增临时工 Tab

## Impact

| 影响范围 | 说明 |
|----------|------|
| `ai-agent-server/` tempworker 模块（新建） | DDD 四层 |
| `ai-agent-server/` user 模块 | TempWorkerService 依赖 UserDomainService 创建用户 |
| 数据库 | 新增 `t_temp_worker` 表；`t_user` 新增 `must_change_password` 字段 |
| 种子数据 | 新增 `temp_worker` 角色 + `user:read` 权限 |
| `ai-agent-client/` ProjectDetailPage | 新增"临时工"Tab |
| `ai-agent-client/` LoginPage | 首页检查 `mustChangePassword` 强制跳转修改密码 |
