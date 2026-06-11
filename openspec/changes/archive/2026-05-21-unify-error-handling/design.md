## Context

当前 `src/api/client.js` 的 axios 响应拦截器仅透传响应体，未处理服务端统一返回的 `{ code, msg, data }` 格式。业务错误码的判断和错误提示分散在 store 中，且文案硬编码在前端。

## Goals / Non-Goals

**Goals:**
- axios 响应拦截器统一处理 `{ code, msg, data }`：`code === 0` 解包 `data`，否则以 `msg` reject
- auth store 移除硬编码错误提示，改用拦截器抛出的 `msg`

**Non-Goals:**
- 不修改 HTTP 错误拦截器（401 重定向）
- 不在组件层引入额外的错误处理逻辑
- 不引入全局 toast/notification 通知机制

## Decisions

**Decision 1: 在响应拦截器（response interceptor）而非错误拦截器（error interceptor）中处理业务错误码**

- **选**: 响应拦截器中判断 `response.data.code`，code !== 0 时 `Promise.reject(new Error(msg))`
- **弃**: 在错误拦截器中处理 — 业务错误通常返回 HTTP 200，不会进入 error interceptor
- **理由**: 服务端业务错误仍返回 HTTP 200，只有通过响应拦截器才能拦截

**Decision 2: Error 对象只携带 msg，不额外保存 code**

- **选**: `new Error(data.msg)`
- **弃**: `const err = new Error(data.msg); err.code = data.code`
- **理由**: 当前所有错误场景仅需展示 msg，暂不需要根据 code 做差异化处理

**Decision 3: 拦截器解包后，调用方直接拿到 data**

- **选**: `response.data = data.data` 替换后返回，store 中 `const data = await client.get(...)` 直接拿到解包后的数据
- **弃**: 保持 `{ code, msg, data }` 结构不变，由每个 store 自行解包
- **理由**: 一次修改，全局生效；新增 store 无需重复解包逻辑

## Risks / Trade-offs

- 现有代码中所有直接访问 `response.data` 的地方需要适配 → 当前仅 `auth.js` 调用 API，影响范围可控
- 调用方无法直接获取 `code` → 当前无此需求，未来如有需要可在 Error 对象上追加 `code` 属性
