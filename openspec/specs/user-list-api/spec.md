## ADDED Requirements

### Requirement: 查询活跃用户列表
系统 SHALL 在 `GET /api/users` 端点返回所有活跃用户（status=ACTIVE）的 ID 和用户名。

#### Scenario: 成功获取用户列表
- **WHEN** 前端请求 `GET /api/users`
- **THEN** 系统返回 HTTP 200，响应体为 `{ "code": 0, "msg": "操作成功", "data": [{ "id": 1, "username": "admin" }, { "id": 2, "username": "张三" }] }`

#### Scenario: 无活跃用户
- **WHEN** 数据库中无 ACTIVE 状态的用户
- **THEN** 系统返回 HTTP 200，`data` 为空数组 `[]`

#### Scenario: 仅返回活跃用户
- **WHEN** 存在 status=DISABLED 的用户
- **THEN** 响应不包含已禁用的用户

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求 `GET /api/users`
- **THEN** 系统返回 HTTP 401

### Requirement: 响应中不包含敏感字段
系统 SHALL NOT 在用户列表响应中返回密码哈希等敏感字段。

#### Scenario: 字段控制
- **WHEN** 系统返回用户列表
- **THEN** 每个用户对象只包含 `id` 和 `username` 字段，不包含 `passwordHash`、`status` 等字段
