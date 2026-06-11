## ADDED Requirements

### Requirement: 国际化配置文件
系统 SHALL 在 `src/main/resources/i18n/` 目录下提供错误消息的国际化配置文件。

#### Scenario: 默认语言配置
- **WHEN** 系统启动
- **THEN** `i18n/messages.properties` 文件 SHALL 存在，包含所有 ErrorCode 对应 messageKey 的中文消息

#### Scenario: 英文语言配置
- **WHEN** 系统需要返回英文错误消息
- **THEN** `i18n/messages_en_US.properties` 文件 SHALL 存在，包含所有 ErrorCode 对应 messageKey 的英文消息

### Requirement: MessageSource 配置
系统 SHALL 配置 Spring `MessageSource`，指定 i18n 资源文件基础路径为 `i18n/messages`，默认编码为 UTF-8。

#### Scenario: 中文消息解析
- **WHEN** 请求的 `Accept-Language` 为 `zh-CN` 或未设置
- **THEN** `MessageSource.getMessage(key, args, locale)` SHALL 返回中文消息文本

#### Scenario: 英文消息解析
- **WHEN** 请求的 `Accept-Language` 为 `en-US` 或 `en`
- **THEN** `MessageSource.getMessage(key, args, locale)` SHALL 返回英文消息文本

### Requirement: 消息占位符支持
错误消息 SHALL 支持 `MessageFormat` 占位符（`{0}`, `{1}` 等），允许在运行时填充动态参数。

#### Scenario: 参数校验失败消息带字段名
- **WHEN** `BusinessException` 以 `ErrorCode.PARAM_INVALID` 和参数 `["用户名不能为空"]` 抛出
- **THEN** 解析后的消息 SHALL 为 `"参数校验失败：用户名不能为空"`（中文）或 `"Validation failed: 用户名不能为空"`（英文）

#### Scenario: 无参数消息
- **WHEN** `BusinessException` 以 `ErrorCode.CREDENTIALS_INVALID` 且无参数抛出
- **THEN** 解析后的消息 SHALL 为 `"用户名或密码错误"`（中文）或 `"Invalid username or password"`（英文）

### Requirement: LocaleResolver 配置
系统 SHALL 使用 `AcceptHeaderLocaleResolver` 根据请求头 `Accept-Language` 自动解析用户语言偏好。

#### Scenario: 未设置 Accept-Language
- **WHEN** 请求未携带 `Accept-Language` 头
- **THEN** 系统 SHALL 使用默认 locale `zh_CN`

#### Scenario: 切换语言
- **WHEN** 请求携带 `Accept-Language: en-US`
- **THEN** 后续错误消息 SHALL 以英文返回
