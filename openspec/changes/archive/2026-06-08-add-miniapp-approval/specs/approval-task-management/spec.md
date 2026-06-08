## ADDED Requirements

### Requirement: 查询待办任务数量

系统 SHALL 在 `GET /api/tasks/count` 端点返回当前登录用户的待办审批任务数量。

#### Scenario: 有待办任务

- **WHEN** 已登录用户请求 `GET /api/tasks/count`
- **THEN** 系统返回 `{"count": 3}`，count 为当前用户待办任务总数

#### Scenario: 无待办任务

- **WHEN** 当前用户没有待办的审批任务
- **THEN** 系统返回 `{"count": 0}`

#### Scenario: 未登录

- **WHEN** 未认证用户请求 `GET /api/tasks/count`
- **THEN** 系统返回 `{"count": 0}`
