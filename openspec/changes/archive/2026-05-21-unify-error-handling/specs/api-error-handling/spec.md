## ADDED Requirements

### Requirement: Axios 响应拦截器统一解包 { code, msg, data }

系统 SHALL 在 axios 响应拦截器中统一处理服务端返回的 `{ code, msg, data }` 格式。当 `code === 0` 时解包 `data` 作为响应数据返回；当 `code !== 0` 时以 `msg` 信息 reject。

#### Scenario: 请求成功（code === 0）

- **WHEN** 服务端返回 `{ code: 0, msg: "ok", data: { id: 1, username: "admin" } }`
- **THEN** 响应拦截器将 `response.data` 替换为 `{ id: 1, username: "admin" }`
- **AND** 调用方直接拿到解包后的数据对象

#### Scenario: 业务错误（code !== 0）

- **WHEN** 服务端返回 `{ code: 1001, msg: "用户名或密码错误", data: null }`
- **THEN** 响应拦截器 reject 一个 Error 对象，其 message 为 `"用户名或密码错误"`
- **AND** 调用方的 catch 分支可捕获该 Error

#### Scenario: HTTP 错误不受影响

- **WHEN** 服务端返回 HTTP 401 且响应体不是 `{ code, msg, data }` 格式
- **THEN** HTTP 错误拦截器继续处理 401 重定向
- **AND** 响应拦截器中的 code 判断不生效
