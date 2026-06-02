## MODIFIED Requirements

### Requirement: 自服务修改密码
系统 SHALL 在 `PUT /api/auth/password` 端点允许已登录用户修改自己的密码，需验证旧密码正确性。修改成功后清除 `t_user.must_change_password` 标记。

#### Scenario: 成功修改密码
- **WHEN** 已登录用户请求 `PUT /api/auth/password`，请求体为 `{ "oldPassword": "old123", "newPassword": "new456" }`
- **THEN** 系统验证旧密码正确，将新密码 BCrypt 哈希后更新到数据库，`must_change_password` 更新为 0，返回操作成功

#### Scenario: 旧密码不正确
- **WHEN** 用户提供的旧密码与数据库中存储的不匹配
- **THEN** 系统返回 `code: 7005`（OLD_PASSWORD_INCORRECT）

#### Scenario: 新密码为空
- **WHEN** 用户请求 `PUT /api/auth/password`，`newPassword` 为空
- **THEN** 系统返回 `code: 1001`（PARAM_MISSING）

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求 `PUT /api/auth/password`
- **THEN** 系统返回 `code: 2001`（NOT_AUTHENTICATED）

## ADDED Requirements

### Requirement: 首次登录强制改密检测
系统 SHALL 在 `GET /api/auth/me` 返回 `mustChangePassword` 字段，前端据此判断是否需要跳转修改密码页面。

#### Scenario: 临时工首次登录返回标记
- **WHEN** 临时工（must_change_password=1）登录后请求 `GET /api/auth/me`
- **THEN** 响应包含 `"mustChangePassword": true`
