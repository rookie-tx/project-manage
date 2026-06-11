## Context

当前项目基于 Spring Boot 3.5.14，Controller 层直接使用 `ResponseEntity<?>` 返回，成功时返回领域对象（如 `LoginResponse`），失败时返回纯文本字符串（如 `"Invalid credentials"`）。已有 `GlobalExceptionHandler` 位于 `login.api` 包中，但实现不完整（`MethodArgumentNotValidException` 处理器返回 null）。项目已有 `spring-boot-starter-validation` 依赖。

## Goals / Non-Goals

**Goals:**
- 所有接口统一返回 `{ code: int, msg: string, data: T|null }` JSON 结构
- HTTP 状态码统一为 200，业务状态通过 `code` 字段区分
- 错误码分区间管理，枚举定义集中在一处
- 错误消息支持国际化（中英文），根据 `Accept-Language` 请求头自动切换
- 业务异常与系统异常统一经由 `GlobalExceptionHandler` 处理

**Non-Goals:**
- 不涉及已有业务逻辑的重构（LoginController 和 SessionController 仅调整返回方式和异常抛出方式）
- 不引入第三方 i18n 库，使用 Spring 原生 `MessageSource`
- 不处理前端适配（前端自行对接新格式）

## Decisions

### 1. 统一 HTTP 200 策略

**决策**: 所有响应（含错误）HTTP 状态码统一返回 200。

**理由**: 国内项目前端通常使用统一的拦截器只解析 body.code，HTTP 状态码反而增加判断分支。统一 200 让前端只需一种处理逻辑。

**替代方案**: 混合模式（400/401/500 各不同）。被否决，因为前端已经需要判断 body.code，HTTP 状态码无额外价值。

### 2. `data` 字段始终序列化

**决策**: 当 data 为 null 时，JSON 中保留 `"data": null`。

**理由**: 前端解析时字段不存在会导致额外的空值判断。始终存在让前端类型定义稳定。

**实现**: `Result` 类上不加 `@JsonInclude(JsonInclude.Include.NON_NULL)`，或显式设置 `JsonInclude.Include.ALWAYS`。

### 3. ErrorCode 使用枚举 + messageKey 模式

**决策**: ErrorCode 枚举中存储 `messageKey`（如 `"auth.credentials_invalid"`），而非硬编码消息文本。错误消息由 `MessageSource` 在 GlobalExceptionHandler 中根据 locale 解析。

**理由**: 消息文本与业务逻辑解耦，支持国际化且不会因为 locale 导致枚举值不一致。

**替代方案**: 枚举中直接存默认消息字符串。被否决，因为无法支持多语言。

### 4. BusinessException 携带 ErrorCode 和可变参数

**决策**: `BusinessException` 构造时接收 `ErrorCode` 和可选的 `Object... args`，用于 `MessageSource.getMessage(key, args, locale)` 的占位符填充。

**允许 MessageFormat 参数场景**: 如 `PARAM_INVALID` 的错误消息为 `"参数校验失败：{0}"`，传入具体字段名后得到 `"参数校验失败：用户名不能为空"`。

### 5. 全局异常处理器移到 common.api

**决策**: `GlobalExceptionHandler` 从 `login.api` 移到 `common.api`，并完善处理逻辑。

**理由**: 异常处理是全局关注点，不适合放在 login 子包中。`common.api` 是全局通用模块的 API 层。

### 6. i18n 配置

**决策**: 
- 使用 Spring Boot 自动配置的 `MessageSource`，通过 `spring.messages.basename=i18n/messages` 指定基础路径
- 默认语言为中文（`spring.web.locale=zh_CN`）
- 使用 `AcceptHeaderLocaleResolver`（Spring Boot 默认），根据请求头 `Accept-Language` 自动切换

**理由**: Spring Boot 原生支持，零额外依赖。`AcceptHeaderLocaleResolver` 是最简单的 locale 解析方式，无需 URL 参数或 Cookie。

**文件位置**: `src/main/resources/i18n/messages.properties`（默认中文）、`src/main/resources/i18n/messages_en_US.properties`

### 7. 包结构

```
cn.tx.aiagent.common
├── api
│   ├── Result.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java    ← 从 login.api 移入
├── exception
│   └── BusinessException.java
└── config
    └── I18nConfig.java（如需要自定义 MessageSource）
```

## Risks / Trade-offs

- **BREAKING 变更**: 响应格式从 `{ userId, username }` 变为 `{ code: 0, msg: "...", data: { userId, username } }`，前端需要全区适配。→ 如果是新项目或前端未上线，影响为零。
- **HTTP 200 掩盖错误**: 反向代理/网关层的监控无法根据 HTTP 状态码统计错误率。→ 可在网关层解析 body.code 做监控，或后续引入自定义 ResponseBodyAdvice 添加 HTTP 状态码。
