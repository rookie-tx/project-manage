## MODIFIED Requirements

### Requirement: 查询活跃用户列表
系统 SHALL 在 `GET /api/users` 端点返回分页用户列表，而非全部活跃用户。端点迁移至 user 模块。响应字段扩展包含 `status`、`roles`、`createdAt`。需指定 `page` 和 `size` 分页参数，支持 `keyword` 和 `status` 筛选。默认不返回已删除用户。

#### Scenario: 默认分页查询
- **WHEN** 管理员请求 `GET /api/users`
- **THEN** 系统返回第 1 页，每页 10 条，`data` 包含 `{ records: [...], total: N, page: 1, size: 10 }`，每条记录包含 `id`、`username`、`status`、`roles`（字符串数组）、`createdAt`

#### Scenario: 仅返回活跃用户
- **WHEN** 管理员请求 `GET /api/users?status=ACTIVE`
- **THEN** 系统仅返回 status 为 ACTIVE 的用户

#### Scenario: 不返回已删除用户
- **WHEN** 存在 is_deleted=1 的用户
- **THEN** 分页结果中不包含已删除用户

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求 `GET /api/users`
- **THEN** 系统返回 `code: 2001`（NOT_AUTHENTICATED）

### Requirement: 响应中不包含敏感字段
系统 SHALL NOT 在用户列表响应中返回密码哈希等敏感字段。

#### Scenario: 字段控制
- **WHEN** 系统返回用户列表
- **THEN** 每个用户记录不包含 `passwordHash` 字段

## REMOVED Requirements

### Requirement: 响应中不包含敏感字段（原简单列表版本）
**Reason**: 端点已迁移至 user 模块并改为分页响应，需包含更多字段（roles、createdAt），原简单字段限制不再适用。新版本 MODIFIED 替换。
**Migration**: 前端原有 `GET /api/users` 调用改为携带分页参数，从 `response.data` 数组取值改为 `response.data.records` 数组取值。
