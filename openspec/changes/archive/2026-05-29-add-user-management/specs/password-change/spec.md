## ADDED Requirements

### Requirement: 自服务修改密码
系统 SHALL 在 `PUT /api/auth/password` 端点允许已登录用户修改自己的密码，需验证旧密码正确性。

#### Scenario: 成功修改密码
- **WHEN** 已登录用户请求 `PUT /api/auth/password`，请求体为 `{ "oldPassword": "old123", "newPassword": "new456" }`
- **THEN** 系统验证旧密码正确，将新密码 BCrypt 哈希后更新到数据库，返回操作成功

#### Scenario: 旧密码不正确
- **WHEN** 用户提供的旧密码与数据库中存储的不匹配
- **THEN** 系统返回 `code: 7005`（OLD_PASSWORD_INCORRECT）

#### Scenario: 新密码与旧密码相同
- **WHEN** 用户提供的 newPassword 等于 oldPassword
- **THEN** 系统返回 `code: 7004`（PASSWORD_SAME_AS_OLD）

#### Scenario: 新密码为空
- **WHEN** 用户请求 `PUT /api/auth/password`，`newPassword` 为空
- **THEN** 系统返回 `code: 1001`（PARAM_MISSING）

#### Scenario: 未认证访问
- **WHEN** 未登录用户请求 `PUT /api/auth/password`
- **THEN** 系统返回 `code: 2001`（NOT_AUTHENTICATED）

### Requirement: 密码强度校验
系统 SHALL 要求新密码长度不少于 6 个字符。

#### Scenario: 新密码过短
- **WHEN** 用户提供的新密码长度小于 6 个字符
- **THEN** 系统返回 `code: 1002`（PARAM_INVALID）

### Requirement: 前端设置页面 — 修改密码
系统 SHALL 在 `/settings` 路由提供设置页面，包含修改密码表单。

#### Scenario: 显示修改密码表单
- **WHEN** 任意已登录用户导航到 `/settings`
- **THEN** 页面显示"修改密码"区域，包含旧密码、新密码、确认新密码三个输入框和提交按钮

#### Scenario: 成功修改密码
- **WHEN** 用户填写正确的旧密码和符合要求的新密码并提交
- **THEN** 系统提示"密码修改成功"，清空表单

#### Scenario: 新密码与确认密码不一致
- **WHEN** 用户填写的新密码与确认新密码不匹配
- **THEN** 前端表单校验提示"两次输入的密码不一致"，不发起请求

#### Scenario: 旧密码错误
- **WHEN** 用户填写的旧密码不正确
- **THEN** 页面显示错误提示"旧密码不正确"
