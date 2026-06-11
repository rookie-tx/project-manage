## Why

当前项目 Controller 层直接返回 `ResponseEntity<?>`，错误响应使用纯文本字符串，没有统一的 JSON 响应格式。随着接口增多，前端需要处理多种不同的响应结构和错误格式，对接成本高且容易出错。需要建立全局统一的响应规范，让所有接口以一致的格式返回数据，并通过国际化机制提供可读的错误提示。

## What Changes

- **新增** `Result<T>` 统一响应包装类，所有接口返回 `{ code, msg, data }` 结构
- **新增** `ErrorCode` 错误码枚举，按业务类别分段管理（1xxx 参数错误、2xxx 认证错误、5xxx 系统错误、9xxx 未知错误）
- **新增** `BusinessException` 业务异常类，携带 ErrorCode 抛出后由全局异常处理器统一转换为 Result
- **新增** JSON 序列化配置，确保 `data: null` 时字段不丢失
- **新增** i18n 国际化配置：`MessageSource` + `LocaleResolver` + `messages.properties` / `messages_en_US.properties`
- **修改** `LoginController` 和 `SessionController`：返回类型从 `ResponseEntity<?>` 改为 `Result<T>`，错误分支用 `BusinessException` 替代手动构造 ResponseEntity
- **移动并重写** `GlobalExceptionHandler`：从 `login.api` 移到 `common.api`，统一处理 `BusinessException`、`MethodArgumentNotValidException`、其他未知异常，全部返回 `Result`
- **BREAKING**：接口响应格式变更，前端需从解析 `ResponseEntity` 改为解析 `Result { code, msg, data }`

## Capabilities

### New Capabilities
- `global-response-format`: 统一响应体格式 — 所有接口返回 `{ code: int, msg: string, data: object|null }`，HTTP 状态码统一 200
- `global-error-code`: 错误码体系 — 分区间枚举管理（0=成功, 1xxx=参数错误, 2xxx=认证授权, 5xxx=系统错误, 9xxx=未知）
- `global-i18n`: 国际化错误消息 — 通过 MessageSource + 配置文件实现中英文错误提示，根据 Accept-Language 请求头自动切换

### Modified Capabilities
<!-- 无现有 capability 被修改 -->

## Impact

- **新增文件**: `common/api/Result.java`, `common/api/ErrorCode.java`, `common/exception/BusinessException.java`, `common/config/I18nConfig.java`, `resources/i18n/messages.properties`, `resources/i18n/messages_en_US.properties`
- **修改文件**: `login/api/GlobalExceptionHandler.java` → 移动并重写至 `common/api/GlobalExceptionHandler.java`, `login/api/LoginController.java`, `login/api/SessionController.java`
- **依赖**: 利用已有的 `spring-boot-starter-web` 和 `spring-boot-starter-validation`，无需新增 Maven 依赖
