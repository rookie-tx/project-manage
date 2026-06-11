## ADDED Requirements

### Requirement: 统一响应体结构
所有 API 接口的响应体 SHALL 采用统一的 JSON 结构：`{ "code": <int>, "msg": "<string>", "data": <object|null> }`。

#### Scenario: 成功响应带数据
- **WHEN** 接口处理成功且有返回数据
- **THEN** 响应体为 `{ "code": 0, "msg": "操作成功", "data": <数据对象> }`，HTTP 状态码为 200

#### Scenario: 成功响应无数据
- **WHEN** 接口处理成功但无返回数据
- **THEN** 响应体为 `{ "code": 0, "msg": "操作成功", "data": null }`，HTTP 状态码为 200

#### Scenario: 业务异常响应
- **WHEN** 接口抛出 `BusinessException`
- **THEN** `GlobalExceptionHandler` SHALL 捕获并返回 `{ "code": <异常对应错误码>, "msg": "<国际化后消息>", "data": null }`，HTTP 状态码为 200

#### Scenario: 参数校验失败响应
- **WHEN** 请求参数 `@Valid` 校验失败抛出 `MethodArgumentNotValidException`
- **THEN** `GlobalExceptionHandler` SHALL 捕获并返回 `{ "code": 1002, "msg": "参数校验失败：<具体字段错误>", "data": null }`，HTTP 状态码为 200

#### Scenario: 未知异常响应
- **WHEN** 系统抛出未预期的 `Exception`
- **THEN** `GlobalExceptionHandler` SHALL 捕获并返回 `{ "code": 9999, "msg": "未知错误", "data": null }`，HTTP 状态码为 200

### Requirement: data 字段始终存在
`Result` 类 SHALL 配置为序列化时始终包含 `data` 字段，即使值为 null。

#### Scenario: data 为 null 时序列化
- **WHEN** `Result.error(code, msg)` 构造的 Result 对象被序列化为 JSON
- **THEN** JSON 中 SHALL 包含 `"data": null`，而非省略该字段

### Requirement: Result 静态工厂方法
`Result<T>` 类 SHALL 提供静态工厂方法用于快速构造响应对象。

#### Scenario: 构造成功响应
- **WHEN** 调用 `Result.success(data)`
- **THEN** 返回 code=0 且 data 为传入值的 Result 对象

#### Scenario: 构造成功响应无数据
- **WHEN** 调用 `Result.success()`
- **THEN** 返回 code=0 且 data 为 null 的 Result 对象

#### Scenario: 构造错误响应
- **WHEN** 调用 `Result.error(code, msg)`
- **THEN** 返回指定 code 和 msg 且 data 为 null 的 Result 对象
