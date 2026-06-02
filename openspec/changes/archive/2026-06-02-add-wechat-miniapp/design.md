## Context

当前系统认证完全依赖 HttpSession（Cookie 传递），`AuthenticationInterceptor` 从 Session 读取 `LoginContext`。小程序不支持 Cookie 持久化，需要 Token-based 认证。同时系统需保持 Web 端 Session 认证不变，两者并行。

微信小程序登录使用 `wx.login()` 获取临时 code，后端通过微信 API `code2Session` 换取 openid 和 session_key。用户通过 openid 与系统用户关联。

uni-app 选用 Vue 3 模式（与 Web 端 Vue 3 + Pinia 技术栈一致），降低团队学习成本。

## Goals / Non-Goals

**Goals:**
- 后端同时支持 Session（Web）和 JWT（小程序）两种认证通道
- 微信用户可通过 `wx.login()` 登录，已绑定用户自动登录，未绑定用户引导绑定已有账号
- 新用户通过微信首次登录时自动创建系统账号（角色默认 `user`）
- JWT 令牌有效期可配置（默认 7 天）
- uni-app 项目初始化，包含登录页、API 封装、Dashboard 骨架
- 微信 appid/secret 配置项可占位，后续替换真实值即可运行

**Non-Goals:**
- 不实现微信手机号授权（Phase 2+）
- 不实现微信支付
- 不实现小程序消息推送/订阅
- 不修改现有 Session 认证逻辑
- 不在 Phase 1 实现业务页面（仅 Dashboard 占位）

## Decisions

### 1. JWT 库选型：jjwt (io.jsonwebtoken)

Spring Security 未引入项目中，不需要其全套认证框架。`jjwt` 是最小侵入的选择——纯 JWT 签发/验证库，配合自定义拦截器即可。

**备选方案**：Nimbus JOSE + JWT — 功能更全但更重；Spring Security OAuth2 — 过度设计，项目当前不依赖 Spring Security。

### 2. 双通道认证：优先级 JWT > Session

`AuthenticationInterceptor` 改造逻辑：

```
请求进入 → 检查 Authorization 头
              │
    ┌─────────┴─────────┐
    │ Bearer token 存在  │  token 不存在
    │ 验证 JWT          │
    │ 解析 LoginContext  │
    │                    │  回退到 Session
    │                    │  读取 HttpSession
    └─────────┬─────────┘
              │
              ▼
        LoginContextHolder.set(ctx)
```

JWT 和 Session 构造的 `LoginContext` 完全一致（userId, username, roles, permissions），后续 `@RequirePermission` 无需感知认证来源。

### 3. 微信登录流程

```
小程序端                         后端                        微信服务器
────────                        ────                        ────────
wx.login() → code
    │
    ├── POST /api/auth/wechat-login {code}
    │                                │
    │                                ├── GET /sns/jscode2session
    │                                │   ?appid=xxx&secret=xxx
    │                                │   &js_code=code
    │                                │                        ──→
    │                                │              {openid, session_key}
    │                                │   ←────────────────────────
    │                                │
    │                                ├── 查 t_user_wechat WHERE openid
    │                                │
    │                    ┌───────────┴───────────┐
    │                    │ 已绑定                 │ 未绑定
    │                    │                       │
    │                    │ 签发 JWT               │ 返回 {needBind: true,
    │                    │ 返回 {token, user}     │        openid}
    │   ←────────────────┤                       │
    │                    │                       
    │   [如果 needBind]
    │   ├── 显示账号绑定表单
    │   ├── POST /api/auth/wechat-bind
    │   │   {code, username, password}
    │   │                    │
    │   │                    ├── 验证用户名密码
    │   │                    ├── INSERT t_user_wechat
    │   │                    ├── 签发 JWT
    │   │                    └── 返回 {token, user}
    │   ←────────────────────┤
    │
    ├── 存储 token 到 uni.storage
    └── 跳转 Dashboard
```

### 4. JWT 载荷设计

```json
{
  "sub": "123",           // userId
  "username": "admin",
  "roles": ["admin"],
  "permissions": ["user:read", "project:read", ...],
  "iat": 1717200000,      // 签发时间
  "exp": 1717804800       // 过期时间 (7天后)
}
```

**为什么不存最小信息（仅 userId）而是存完整角色权限？** 避免每次请求都查数据库。小程序端每次 `wx.request` 都携带 token，如果每次后端都要连表查角色权限，开销太大。角色权限变更时，用户需重新登录（可接受，7 天过期）。

**权衡**：角色/权限变更后旧 token 仍然有效直到过期。对于企业内部管理系统，7 天窗口内权限变更不即时生效是可接受的。

### 5. 新用户自动注册

当 openid 未绑定时，如果用户选择"微信一键注册"（Phase 2），系统自动创建：
- `t_user`：username = `wx_{openid前8位}`，随机密码（不可用于 Web 登录），角色 = `user`
- `t_user_wechat`：绑定关系

Phase 1 仅实现"已有账号绑定微信"模式，自动注册留到 Phase 2。

### 6. uni-app 项目结构

```
ai-agent-miniapp/
├── src/
│   ├── api/
│   │   ├── client.js          # uni.request 封装 (类似 Web client.js)
│   │   └── auth.js            # 微信登录/绑定 API
│   ├── stores/
│   │   └── auth.js            # Pinia auth store (Token 管理)
│   ├── pages/
│   │   ├── login/
│   │   │   └── index.vue      # 登录页 (微信授权 + 账号绑定)
│   │   └── dashboard/
│   │       └── index.vue      # Dashboard 首页
│   ├── components/            # 可复用组件 (Phase 1 可空)
│   ├── utils/
│   │   └── storage.js         # Token 存储 (uni.storage)
│   ├── App.vue
│   └── main.js
├── pages.json                 # 页面路由
├── manifest.json              # 小程序配置 (appid占位)
├── uni.scss                   # 全局样式
└── package.json
```

注意：uni-app 使用 `pages.json` 管理路由，不使用 Vue Router。页面路径即路由。

### 7. API 请求封装

与 Web 端 `client.js` 对齐的模式：

```javascript
// api/client.js
const client = {
  async request(method, url, data) {
    const token = uni.getStorageSync('token')
    const res = await uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      },
    })
    const { code, msg, data: resData } = res.data
    if (code === 0) return resData
    if (code === 2004 || code === 2001) {
      // token 过期或未认证 → 跳转登录
      uni.removeStorageSync('token')
      uni.reLaunch({ url: '/pages/login/index' })
      throw new Error('登录已过期')
    }
    throw new Error(msg)
  },
  get(url) { return this.request('GET', url) },
  post(url, data) { return this.request('POST', url, data) },
  delete(url) { return this.request('DELETE', url) },
}
```

### 8. 后端新增配置项

```yaml
app:
  jwt:
    secret: changeme-please-change-in-production
    expiration-days: 7
  wechat:
    miniapp:
      appid: placeholder-your-appid
      secret: placeholder-your-secret
```

## Risks / Trade-offs

- **[JWT 无状态吊销]** JWT 签发后无法即时失效。→ 通过短有效期（7天）缓解；后续可引入 refresh token 或黑名单机制
- **[JWT 载荷膨胀]** 角色权限存在 token 中 → 限制 permissions 长度不超过 token 合理范围（当前系统权限 < 50 个，可接受）
- **[小程序审核]** uni-app 小程序须通过微信审核才能上线 → 不影响开发，真实 appid 申请后替换即可
- **[Session 与 JWT 并发安全]** 两者操作同一 `LoginContextHolder`（ThreadLocal），无冲突——每个请求只走一条通道
- **[新用户注册安全]** Phase 2 微信一键注册时需防刷 → 可加入图形验证码或频率限制（Phase 2 处理）
