## Why

服务端统一返回 `{ code, msg, data }` 格式的错误码，当前前端未对响应体中的业务错误码做任何处理，且错误提示文案硬编码在前端。需要在 axios 层统一拦截处理，让所有业务错误都能通过服务端返回的 `msg` 精准提示用户。

## What Changes

- axios 响应拦截器中新增 `code` 判断：`code === 0` 时解包返回 `data`，否则以 `msg` 信息 reject
- auth store 移除硬编码的错误提示文案，改用拦截器抛出的 `msg`
- HTTP 级别的 401 拦截器保持不变

## Capabilities

### New Capabilities
- `api-error-handling`: 统一处理服务端返回的 `{ code, msg, data }` 响应格式，code 为 0 时解包 data，其余情况以 msg 文案 reject

### Modified Capabilities
- `auth`: 登录错误提示文案从硬编码改为来自服务端返回的 msg

## Impact

- `src/api/client.js` — 新增响应拦截器逻辑
- `src/stores/auth.js` — `login()` 移除硬编码错误字符串，`fetchUser()` 适配解包后的响应
