## ADDED Requirements

### Requirement: 用户拥有真实姓名字段
系统 SHALL 在 `t_user` 表中存储用户的真实姓名。`name` 字段可为空，未设置时前端降级显示 `username`。

#### Scenario: 临时工创建时填写名字
- **WHEN** 管理员提交 `POST /api/temp-workers` 包含 `{ "name": "张三", "phone": "13800138000", ... }`
- **THEN** 系统创建 `t_user` 记录，`name` 为 "张三"，`username` 为 "13800138000"

#### Scenario: name 为 null 时降级显示
- **WHEN** 某用户 `name` 字段为 null
- **THEN** 前端展示其 `username`（手机号）代替名字

### Requirement: 认证上下文携带姓名字段
系统 SHALL 在 `LoginContext` 中携带 `name` 字段，使得 `/api/auth/me` 响应和 JWT 令牌均包含 name。

#### Scenario: 登录后获取用户信息含名字
- **WHEN** 用户已登录并请求 `GET /api/auth/me`
- **THEN** 响应包含 `name` 字段，值为 `t_user.name`（可为 null）

#### Scenario: JWT 令牌包含 name
- **WHEN** 系统为用户签发 JWT 令牌
- **THEN** 令牌 payload 包含 `name` claim，值为 `t_user.name`

### Requirement: 临时工创建需提供名字
系统 SHALL 在 `POST /api/temp-workers` 接口中要求必填 `name` 字段。

#### Scenario: 创建时缺少名字
- **WHEN** 提交 `POST /api/temp-workers` 不包含 `name` 字段
- **THEN** 系统返回参数校验错误

#### Scenario: 成功创建的响应
- **WHEN** 提交 `POST /api/temp-workers` 包含合法 `name`
- **THEN** 返回 HTTP 200，`code` 为 0

### Requirement: 用户编辑接口可修改名字
系统 SHALL 在 `PUT /api/users/:id` 接口中支持修改 `name` 字段。

#### Scenario: 通过用户编辑接口修改名字
- **WHEN** 管理员提交 `PUT /api/users/123` 包含 `{ "name": "李四" }`
- **THEN** `t_user.name` 更新为 "李四"

### Requirement: 审批任务完成时记录审批人名字
系统 SHALL 在 `completeTask` 时将当前审批人的 `name`（非 `username`）作为 `assigneeName` 存入 Flowable 任务变量。

#### Scenario: 审批人名字存储
- **WHEN** 用户 "张三"（name=张三, username=13800138000）审批通过一个任务
- **THEN** Flowable 任务局部变量 `assigneeName` 的值为 "张三"

#### Scenario: 审批人名字为 null 时存储 userId
- **WHEN** 审批人 `name` 为 null
- **THEN** 前端审批历史通过 `assigneeName || assignee` 降级展示 userId

### Requirement: 临时工列表返回名字
系统 SHALL 在 `GET /api/projects/:id/temp-workers` 响应中返回 `name` 字段。

#### Scenario: 临时工列表含名字
- **WHEN** 请求某项目的临时工列表
- **THEN** 响应中每个临时工对象包含 `name` 字段，值来自 `t_user.name`
