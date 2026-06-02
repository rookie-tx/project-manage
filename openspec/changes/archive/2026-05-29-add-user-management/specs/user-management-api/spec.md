## ADDED Requirements

### Requirement: 分页查询用户列表
系统 SHALL 在 `GET /api/users` 端点返回分页用户列表，包含用户基本信息、状态和角色。

#### Scenario: 默认分页查询
- **WHEN** 管理员请求 `GET /api/users`
- **THEN** 系统返回第 1 页，每页 10 条，`data` 包含 `{ records: [...], total: N, page: 1, size: 10 }`，每条记录包含 `id`、`username`、`status`（ACTIVE/DISABLED）、`roles`（字符串数组）、`createdAt`

#### Scenario: 指定分页参数
- **WHEN** 管理员请求 `GET /api/users?page=2&size=20`
- **THEN** 系统返回第 2 页，每页 20 条记录

#### Scenario: 按关键字搜索
- **WHEN** 管理员请求 `GET /api/users?keyword=admin`
- **THEN** 系统返回 username 包含 "admin" 的用户分页列表

#### Scenario: 按状态筛选
- **WHEN** 管理员请求 `GET /api/users?status=ACTIVE`
- **THEN** 系统只返回 status 为 ACTIVE 的用户

#### Scenario: 不返回已删除用户
- **WHEN** 存在 is_deleted=1 的用户
- **THEN** 响应不包含已删除的用户

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求 `GET /api/users`
- **THEN** 系统返回 HTTP 200，`code` 为 2001（NOT_AUTHENTICATED）

### Requirement: 查询用户详情
系统 SHALL 在 `GET /api/users/{id}` 端点返回单个用户的详细信息，包含角色列表。

#### Scenario: 成功获取用户详情
- **WHEN** 管理员请求 `GET /api/users/1`
- **THEN** 系统返回 `{ id: 1, username: "admin", status: "ACTIVE", roles: ["管理员"], createdAt: "..." }`

#### Scenario: 用户不存在
- **WHEN** 管理员请求 `GET /api/users/999`
- **THEN** 系统返回 `code: 7001`（USER_NOT_FOUND）

#### Scenario: 用户已删除
- **WHEN** 管理员请求已被软删除的用户详情
- **THEN** 系统返回 `code: 7003`（USER_DELETED）

### Requirement: 创建用户
系统 SHALL 在 `POST /api/users` 端点允许管理员创建新用户并可选分配角色。

#### Scenario: 成功创建用户并分配角色
- **WHEN** 管理员请求 `POST /api/users`，请求体为 `{ "username": "zhangsan", "password": "123456", "roleIds": [2, 3] }`
- **THEN** 系统创建用户 zhangsan（status=ACTIVE），密码 BCrypt 哈希存储，关联角色 2 和 3，返回 `{ id: N, username: "zhangsan" }`

#### Scenario: 创建用户不分配角色
- **WHEN** 管理员请求 `POST /api/users`，请求体为 `{ "username": "lisi", "password": "123456" }`
- **THEN** 系统创建用户 lisi，不关联任何角色

#### Scenario: 用户名为空
- **WHEN** 管理员请求 `POST /api/users`，`username` 为空
- **THEN** 系统返回 `code: 1001`（PARAM_MISSING）

#### Scenario: 用户名重复
- **WHEN** 管理员请求创建已存在的 username
- **THEN** 系统返回 `code: 7002`（USERNAME_DUPLICATE）

### Requirement: 编辑用户
系统 SHALL 在 `PUT /api/users/{id}` 端点允许管理员编辑用户信息和重新分配角色。

#### Scenario: 成功编辑用户并重设角色
- **WHEN** 管理员请求 `PUT /api/users/1`，请求体为 `{ "status": "DISABLED", "roleIds": [1] }`
- **THEN** 系统更新用户状态为 DISABLED，角色替换为仅角色 1

#### Scenario: 编辑不存在的用户
- **WHEN** 管理员请求编辑不存在的用户 ID
- **THEN** 系统返回 `code: 7001`（USER_NOT_FOUND）

### Requirement: 软删除用户
系统 SHALL 在 `DELETE /api/users/{id}` 端点将用户标记为已删除（is_deleted=1），而非物理删除。

#### Scenario: 成功软删除用户
- **WHEN** 管理员请求 `DELETE /api/users/5`
- **THEN** 系统将用户 5 的 is_deleted 设为 1，后续查询不再返回该用户

#### Scenario: 删除不存在的用户
- **WHEN** 管理员请求删除不存在的用户 ID
- **THEN** 系统返回 `code: 7001`（USER_NOT_FOUND）

### Requirement: 响应不包含密码哈希
系统 SHALL NOT 在任何用户管理端点响应中返回 `passwordHash` 字段。

#### Scenario: 用户列表不包含密码
- **WHEN** 系统返回用户列表或详情
- **THEN** 响应中不包含 `passwordHash` 或 `password` 字段
