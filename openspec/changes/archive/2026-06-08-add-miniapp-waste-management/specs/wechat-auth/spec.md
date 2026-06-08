## MODIFIED Requirements

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
