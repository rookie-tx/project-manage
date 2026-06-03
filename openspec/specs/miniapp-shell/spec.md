## ADDED Requirements

### Requirement: uni-app 项目骨架

系统 SHALL 提供 uni-app 微信小程序项目 `ai-agent-miniapp/`，包含完整的项目结构、路由配置和基础页面。

#### Scenario: 项目可正常编译

- **WHEN** 开发者执行 `npm run dev:mp-weixin`（或其他启动命令）
- **THEN** uni-app 项目成功编译，生成微信小程序代码

#### Scenario: 页面路由配置正确

- **WHEN** 小程序启动
- **THEN** 默认进入登录页 `/pages/login/index`，登录成功后跳转到 Dashboard `/pages/dashboard/index`

### Requirement: 微信登录页

小程序 SHALL 提供登录页面，支持微信一键授权登录和已有账号绑定两种模式。

#### Scenario: 微信授权登录成功

- **WHEN** 用户点击"微信一键登录"按钮且 `wx.login()` 成功获取 code
- **THEN** 小程序调用 `/api/auth/wechat-login`，若已绑定则自动登录并跳转 Dashboard

#### Scenario: 引导账号绑定

- **WHEN** 微信登录接口返回 `needBind: true`
- **THEN** 页面展示用户名和密码输入框，用户填写后点击"绑定并登录"提交到 `/api/auth/wechat-bind`

#### Scenario: 绑定失败提示

- **WHEN** 用户输入错误的用户名或密码
- **THEN** 页面显示错误提示"用户名或密码错误"，不跳转

#### Scenario: 登录状态保持

- **WHEN** 用户成功登录后关闭小程序再打开
- **THEN** 小程序从 `uni.storage` 读取 token，若无过期则直接进入 Dashboard，无需重新登录

### Requirement: API 请求封装

小程序 SHALL 提供统一的 HTTP 请求封装，自动附加 JWT Token 并处理认证异常。

#### Scenario: 自动附加 Authorization 头

- **WHEN** 已登录用户发起任意 API 请求
- **THEN** 请求头自动包含 `Authorization: Bearer <token>`

#### Scenario: Token 过期自动跳转登录

- **WHEN** API 请求返回认证错误（code 为 2001 或 2004）
- **THEN** 系统清除本地 token 存储，跳转到登录页

#### Scenario: 业务错误透传

- **WHEN** API 请求返回非 0 业务错误码（如参数校验错误）
- **THEN** 请求封装 reject 对应的 `Error(msg)`，由调用方处理

### Requirement: Dashboard 首页

小程序 SHALL 提供 Dashboard 首页，展示当前用户信息和功能入口。

#### Scenario: 展示用户信息

- **WHEN** 已登录用户进入 Dashboard
- **THEN** 页面显示用户名和当前角色信息

#### Scenario: 基础布局

- **WHEN** 用户查看 Dashboard 页面
- **THEN** 展示顶部导航栏（标题"智能管理平台"）和功能入口网格（考勤打卡、项目管理等）

#### Scenario: 考勤打卡入口可跳转

- **WHEN** 用户点击"考勤打卡"功能入口
- **THEN** 跳转到打卡页面 `/pages/checkin/checkin`
