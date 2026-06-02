## Why

当前系统仅有 Web 端（Vue 3 SPA），现场人员（临时工、项目经理、司机）在工地操作不便——打卡、废渣拍照上传、审批处理等场景需要移动端支持。新增微信小程序作为第二客户端，覆盖移动端高频场景，提升现场工作效率。

## What Changes

- **新增 JWT 认证通道**：在现有 Session 认证基础上并行支持 JWT Token 认证，小程序通过 `Authorization: Bearer <token>` 头携带身份信息
- **新增微信登录与绑定**：小程序通过 `wx.login()` 获取 code，后端换取 openid，支持已有用户绑定微信和新用户自动注册
- **新增 `t_user_wechat` 表**：存储用户与微信 openid 的绑定关系
- **新增 uni-app 小程序项目**：`ai-agent-miniapp/`，Vue 3 + Pinia 技术栈
- **Phase 1 仅含认证框架 + Dashboard 骨架**：登录页（微信授权 + 账号绑定）、Token 管理、API 请求封装、Dashboard 首页
- 新用户通过微信注册时自动创建 `t_user` 记录，默认角色为 `user`
- JWT 有效期默认 7 天，可通过配置项调整
- 微信 appid/secret 先使用占位值，待后续申请后配置

## Capabilities

### New Capabilities

- `wechat-auth`: 微信认证能力 — JWT 令牌签发与验证、微信 code→openid 交换、用户绑定/解绑、双通道（Session + JWT）认证拦截、新用户自动注册
- `miniapp-shell`: 小程序骨架能力 — uni-app 项目结构、微信登录页（授权 + 账号绑定）、Token 存储与自动携带、API 请求拦截器（401 处理）、Dashboard 首页布局

### Modified Capabilities

<!-- 不修改任何现有 spec。Session 认证行为保持不变，JWT 是并行新增通道 -->

## Impact

- **数据库**：新增 `t_user_wechat` 表（user_id, openid, unionid, nickname, avatar_url）；`schema.sql` 新增建表语句和序列校准
- **后端依赖**：新增 `io.jsonwebtoken:jjwt-api`（JWT）、`io.jsonwebtoken:jjwt-impl`、`io.jsonwebtoken:jjwt-jackson`
- **后端新增模块**：`cn.tx.aiagent.wechat`（微信登录/绑定 API、WechatApiClient）、`cn.tx.aiagent.common.security.JwtTokenProvider`
- **后端修改**：`WebMvcConfig` 注册 JWT 拦截器、`application.yml` 新增 wechat + jwt 配置段、`ErrorCode` 新增微信相关错误码
- **新增 API**：`POST /api/auth/wechat-login`、`POST /api/auth/wechat-bind`、`DELETE /api/auth/wechat-unbind`
- **新增项目**：`ai-agent-miniapp/`（uni-app 小程序），与现有 `ai-agent-client/` 并列
- **前端依赖**：uni-app CLI + Pinia + uView UI（或 uni-ui）
