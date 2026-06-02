## Why

临时工已可登录系统，但缺少核心的打卡功能。临时工需要通过打卡记录上下班时间，管理员需要查看项目下临时工的打卡情况。

## What Changes

- 新建 **attendance 模块**，提供打卡 API（上班打卡/下班打卡/查询记录）
- 数据库新增 `t_attendance` 表（worker_id, project_id, clock_in, clock_out, status）
- 种子数据新增 `attendance:clock-in`、`attendance:clock-out` 权限，temp_worker 角色关联
- 临时工登录后重定向到打卡页面（而非 Dashboard）
- 前端新增 `AttendancePage.vue`（打卡按钮 + 当月记录表）
- 项目详情页"临时工"Tab 新增打卡记录查看

## Capabilities

### New Capabilities
- `attendance`: 上班打卡/下班打卡/当月记录查询/项目维度的打卡记录管理

## Impact

| 影响范围 | 说明 |
|----------|------|
| `ai-agent-server/` attendance 模块（新建） | DDD 四层 |
| 数据库 | 新增 `t_attendance` 表 |
| 种子数据 | 新增 2 条权限 + temp_worker 角色关联 |
| `ai-agent-client/` router | 新增 `/attendance` 路由 |
| `ai-agent-client/` App.vue | 临时工登录后跳转打卡页 |
| `ai-agent-client/` TempWorkerTab | 新增打卡记录 |
