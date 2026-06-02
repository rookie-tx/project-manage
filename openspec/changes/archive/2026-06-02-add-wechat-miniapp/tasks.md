## 1. 数据库变更

- [x] 1.1 在 `schema.sql` 中新增 `t_user_wechat` 建表语句（user_id FK→t_user, openid UNIQUE, unionid, nickname, avatar_url, created_at）
- [x] 1.2 在 `schema.sql` 末尾新增 `t_user_wechat_id_seq` 的 `setval` 序列校准

## 2. 后端 — 依赖与配置

- [x] 2.1 在 `pom.xml` 中新增 jjwt 依赖（jjwt-api、jjwt-impl、jjwt-jackson）
- [x] 2.2 在 `application.yml` 中新增 JWT 和微信小程序配置段（`app.jwt.secret`、`app.jwt.expiration-days`、`app.wechat.miniapp.appid`、`app.wechat.miniapp.secret`）

## 3. 后端 — JWT 基础设施 (cn.tx.aiagent.common.security)

- [x] 3.1 创建 `JwtTokenProvider.java` — JWT 签发（createToken）和验证（validateToken）方法，载荷包含 userId/username/roles/permissions
- [x] 3.2 创建 `JwtProperties.java` — `@ConfigurationProperties(prefix="app.jwt")` 读取 secret 和 expiration-days
- [x] 3.3 创建 `JwtAuthenticationFilter.java` — 检查 `Authorization: Bearer <token>` 头，验证 JWT，注入 `LoginContextHolder`。无 token 时放行（回退 Session）

## 4. 后端 — 微信模块领域层 (cn.tx.aiagent.wechat)

- [x] 4.1 创建 `domain/WechatUserBinding.java` — 领域实体（userId, openid, unionid, nickname, avatarUrl, createdAt），静态工厂方法 `create` 和 `fromPersistence`
- [x] 4.2 创建 `domain/WechatUserBindingRepository.java` — 仓储接口（save, findByOpenid, findByUserId, deleteByUserId）
- [x] 4.3 创建 `WechatProperties.java` — `@ConfigurationProperties(prefix="app.wechat.miniapp")` 读取 appid/secret
- [x] 4.4 创建 `WechatApiClient.java` — 调用微信 `code2Session` API 换取 openid，HTTP 请求使用 `RestTemplate` 或 `HttpClient`

## 5. 后端 — 微信模块基础设施层 (cn.tx.aiagent.wechat.infrastructure)

- [x] 5.1 创建 `WechatUserBindingPO.java` — 持久化对象
- [x] 5.2 创建 `WechatUserBindingPoMapper.java` — MapStruct PO↔Domain 映射器
- [x] 5.3 创建 `WechatUserBindingMapper.java` — MyBatis Mapper 接口（insert, selectByOpenid, selectByUserId, deleteByUserId）
- [x] 5.4 创建 `WechatUserBindingMapper.xml` — MyBatis SQL 映射文件
- [x] 5.5 创建 `WechatUserBindingRepositoryImpl.java` — 仓储实现（@Component）

## 6. 后端 — 微信认证 API (cn.tx.aiagent.wechat.api)

- [x] 6.1 创建 `WechatLoginRequest.java` — 请求 DTO（code 字段）
- [x] 6.2 创建 `WechatBindRequest.java` — 请求 DTO（code, username, password 字段）
- [x] 6.3 创建 `WechatLoginResponse.java` — 响应 DTO（token, user 信息, needBind 标志）
- [x] 6.4 创建 `WechatAuthController.java` — 三个端点：POST /wechat-login、POST /wechat-bind、DELETE /wechat-unbind。wechat-login 中调用 WechatApiClient 换取 openid，已绑定则签发 JWT，未绑定返回 needBind；wechat-bind 中验证用户名密码后创建绑定并签发 JWT；wechat-unbind 中删除绑定关系

## 7. 后端 — 认证拦截器改造

- [x] 7.1 修改 `WebMvcConfig.java` — 在 `addInterceptors` 中注册 `JwtAuthenticationFilter`（置于 AuthenticationInterceptor 之前），并排除 `/api/auth/wechat-login` 和 `/api/auth/wechat-bind` 路径
- [x] 7.2 修改 `AuthenticationInterceptor.java` — 增加判断：如果 `LoginContextHolder` 已有值（来自 JWT Filter），则跳过 Session 读取
- [x] 7.3 在 `ErrorCode.java` 中新增微信相关错误码（WECHAT_CODE_INVALID, WECHAT_API_ERROR, WECHAT_ALREADY_BOUND, WECHAT_NOT_BOUND）
- [x] 7.4 在 `i18n/messages.properties` 和 `messages_en_US.properties` 中新增微信相关错误消息

## 8. 小程序 — 项目初始化

- [x] 8.1 使用 uni-app CLI 创建 `ai-agent-miniapp/` 项目（Vue 3 模板），或手动搭建项目骨架
- [x] 8.2 配置 `manifest.json` — 填写微信小程序 appid（占位值）、设置上传时自动压缩等
- [x] 8.3 配置 `pages.json` — 定义登录页和 Dashboard 页的路由，设置导航栏样式
- [x] 8.4 安装依赖：Pinia（状态管理）

## 9. 小程序 — API 与工具层

- [x] 9.1 创建 `src/utils/storage.js` — Token 存储工具（getToken, setToken, removeToken，使用 uni.storage）
- [x] 9.2 创建 `src/api/client.js` — uni.request 封装（自动附加 Authorization 头、code===0 解包、认证错误 401 处理跳转登录），BASE_URL 指向后端地址
- [x] 9.3 创建 `src/api/auth.js` — 微信登录/绑定/解绑 API 函数（wechatLogin, wechatBind, wechatUnbind）

## 10. 小程序 — Auth Store

- [x] 10.1 创建 `src/stores/auth.js` — Pinia Setup Store（token, user, isLoggedIn, login, bindAccount, logout, checkToken），checkToken 在 App.vue onLaunch 时调用

## 11. 小程序 — 页面

- [x] 11.1 创建 `src/pages/login/index.vue` — 登录页：微信一键登录按钮（调用 wx.login → wechatLogin API）；账号绑定表单（needBind 时显示用户名密码输入框 → wechatBind API）；错误提示
- [x] 11.2 创建 `src/pages/dashboard/index.vue` — Dashboard 首页：用户名显示、角色信息、基础布局框架（后续迭代添加功能入口）
- [x] 11.3 修改 `src/App.vue` — onLaunch 中调用 auth.checkToken() 检查登录状态，根据结果跳转登录页或 Dashboard

## 12. 验证

- [x] 12.1 编译后端：`./mvnw compile` 确认 JWT、微信模块代码无编译错误
- [x] 12.2 运行后端：启动服务，验证 `t_user_wechat` 表自动创建
- [x] 12.3 测试 JWT 签发与验证：调用 wechat-login API（模拟或使用测试 code）验证 Token 生成和后续请求认证
- [x] 12.4 编译小程序：执行 `npm run dev:mp-weixin` 确认项目编译成功
- [x] 12.5 端到端测试：小程序登录页 → 微信登录 → Dashboard 流程（使用微信开发者工具 + Mock code）
