## ADDED Requirements

### Requirement: 供应商一键注册
系统 SHALL 允许微信小程序用户通过微信授权后填写企业信息，一步完成账号注册、微信绑定和供应商资料创建。

#### Scenario: 注册成功
- **WHEN** 小程序用户调用 `wx.login()` 获取微信 code，提交 `POST /api/supplier/register`，包含 code、公司名称、统一社会信用代码、联系人、联系电话、地址
- **THEN** 系统通过 code 换取 openid，自动创建 User（分配 supplier 角色）、创建 WechatUserBinding 绑定、创建 Supplier 记录，返回 JWT token、用户信息和供应商信息

#### Scenario: 微信 code 无效
- **WHEN** 提交的 code 已过期或无效
- **THEN** 系统返回微信 code 无效错误

#### Scenario: openid 已绑定
- **WHEN** 提交的 code 对应的 openid 已在 `t_user_wechat` 中存在绑定记录
- **THEN** 系统返回"该微信已注册"错误，提示用户直接登录

#### Scenario: 必填字段缺失
- **WHEN** 提交的请求中公司名称、联系人或联系电话为空
- **THEN** 系统返回参数校验错误，指出缺失字段

#### Scenario: 注册后用户名唯一
- **WHEN** 系统自动生成的 username 与现有用户冲突
- **THEN** 系统在 username 后追加随机后缀重试，确保创建成功

### Requirement: 注册接口无需认证
注册接口为公开接口，SHALL 不要求调用方携带 JWT token。

#### Scenario: 未认证用户调用注册
- **WHEN** 未登录小程序用户调用 `POST /api/supplier/register`
- **THEN** 系统正常处理注册请求，不返回认证错误

### Requirement: 注册即登录
注册成功后 SHALL 直接返回有效的 JWT token，用户无需再次登录。

#### Scenario: 注册后获得 token
- **WHEN** 注册成功
- **THEN** 返回的 token 可正常用于后续 API 调用，用户权限包含 `supplier:profile`
