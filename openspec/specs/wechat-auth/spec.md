## ADDED Requirements

### Requirement: 小程序微信登录

系统 SHALL 支持小程序通过微信 code 登录。已绑定微信的用户自动登录并返回 JWT 令牌和用户权限，未绑定的用户返回绑定引导状态。

#### Scenario: 已绑定用户登录成功

- **WHEN** 小程序调用 `POST /api/auth/wechat-login` 携带有效 `code`，且对应 openid 已在 `t_user_wechat` 中存在绑定关系
- **THEN** 系统通过微信 API 换取 openid，查找绑定用户，签发 JWT 令牌，返回 `{"token": "<jwt>", "user": {"id": 1, "username": "admin", "permissions": ["waste:read", "waste:create", "waste:delete"]}}`

#### Scenario: 未绑定用户返回引导

- **WHEN** 小程序调用 `POST /api/auth/wechat-login` 携带有效 `code`，但对应 openid 未绑定任何用户
- **THEN** 系统返回 `{"needBind": true}`，小程序展示账号绑定表单

#### Scenario: code 无效或已过期

- **WHEN** 小程序调用 `POST /api/auth/wechat-login` 携带无效或已过期的 `code`
- **THEN** 系统返回错误码 `WECHAT_CODE_INVALID`

#### Scenario: 微信 API 不可用

- **WHEN** 后端调用微信 `code2Session` 接口失败（网络异常或微信服务异常）
- **THEN** 系统返回错误码 `WECHAT_API_ERROR`

### Requirement: 账号绑定微信

系统 SHALL 允许已有系统账号的用户通过用户名密码验证后绑定微信 openid。

#### Scenario: 绑定成功

- **WHEN** 用户调用 `POST /api/auth/wechat-bind` 携带有效 `code`、正确的 `username` 和 `password`
- **THEN** 系统验证用户名密码通过后将 openid 与用户关联写入 `t_user_wechat`，签发 JWT 令牌并返回

#### Scenario: 用户名密码错误

- **WHEN** 用户调用 `POST /api/auth/wechat-bind` 但用户名或密码不正确
- **THEN** 系统返回错误码 `CREDENTIALS_INVALID`，不创建绑定关系

#### Scenario: 账号已被禁用

- **WHEN** 用户调用 `POST /api/auth/wechat-bind` 但该账号状态为禁用
- **THEN** 系统返回错误码 `ACCOUNT_DISABLED`

#### Scenario: openid 已绑定其他账号

- **WHEN** 用户调用 `POST /api/auth/wechat-bind` 但该 openid 已经绑定了另一个用户
- **THEN** 系统返回错误码 `WECHAT_ALREADY_BOUND`

### Requirement: 解除微信绑定

系统 SHALL 允许已认证用户解除其账号与微信的绑定关系。

#### Scenario: 解绑成功

- **WHEN** 已认证用户调用 `DELETE /api/auth/wechat-unbind`
- **THEN** 系统删除 `t_user_wechat` 中该用户对应的记录，返回成功

#### Scenario: 未绑定用户解绑

- **WHEN** 未绑定微信的用户调用 `DELETE /api/auth/wechat-unbind`
- **THEN** 系统返回错误码 `WECHAT_NOT_BOUND`

### Requirement: JWT Token 认证

系统 SHALL 支持通过 `Authorization: Bearer <token>` 请求头进行身份认证，JWT 验证通过后构造与 Session 认证一致的 `LoginContext`。

#### Scenario: 有效 Token 认证通过

- **WHEN** 请求携带有效的 JWT 令牌在 `Authorization` 头中
- **THEN** 系统解析令牌中的 userId、username、roles、permissions 并注入 `LoginContextHolder`，后续 `@RequirePermission` 正常鉴权

#### Scenario: Token 过期

- **WHEN** 请求携带已过期的 JWT 令牌
- **THEN** 系统返回错误码 `SESSION_EXPIRED`（复用现有会话过期错误码）

#### Scenario: Token 签名无效

- **WHEN** 请求携带签名无效或被篡改的 JWT 令牌
- **THEN** 系统返回错误码 `NOT_AUTHENTICATED`

#### Scenario: 无 Token 回退 Session

- **WHEN** 请求不携带 `Authorization` 头
- **THEN** 系统回退到现有 Session 认证逻辑，行为不变（保持 Web 端兼容）

### Requirement: JWT Token 签发

系统 SHALL 在用户认证成功后签发 JWT 令牌，令牌包含用户身份、角色和权限信息，有效期可配置。

#### Scenario: Token 包含完整信息

- **WHEN** 系统签发 JWT 令牌
- **THEN** 令牌负载包含 `sub`（userId）、`username`、`roles`（角色码列表）、`permissions`（权限码列表）、`iat`（签发时间）、`exp`（过期时间）

#### Scenario: Token 有效期可配置

- **WHEN** 管理员修改 `app.jwt.expiration-days` 配置项
- **THEN** 新签发的 JWT 令牌使用新的有效期
