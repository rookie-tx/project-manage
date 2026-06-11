## ADDED Requirements

### Requirement: 错误码分区间管理
系统 SHALL 以 int 类型错误码标识业务状态，0 表示成功，非 0 表示错误。错误码 SHALL 按以下区间划分：

| 区间 | 类别 | 示例 |
|------|------|------|
| 0 | 成功 | SUCCESS |
| 1xxx | 参数/校验错误 | PARAM_MISSING, PARAM_INVALID, PARAM_TYPE_ERROR |
| 2xxx | 认证/授权错误 | NOT_AUTHENTICATED, CREDENTIALS_INVALID, ACCOUNT_DISABLED |
| 5xxx | 系统/服务器错误 | INTERNAL_ERROR, DATABASE_ERROR, REMOTE_CALL_ERROR |
| 9xxx | 未知错误 | UNKNOWN_ERROR |

#### Scenario: 成功状态码
- **WHEN** 业务处理成功
- **THEN** `ErrorCode.SUCCESS.getCode()` SHALL 返回 0

#### Scenario: 参数错误区间
- **WHEN** 请求参数校验失败
- **THEN** 返回的错误码 SHALL 在 1000-1999 范围内

#### Scenario: 认证错误区间
- **WHEN** 认证或授权失败
- **THEN** 返回的错误码 SHALL 在 2000-2999 范围内

### Requirement: ErrorCode 枚举定义
系统 SHALL 定义 `ErrorCode` 枚举，每个枚举值包含 `int code` 和 `String messageKey` 两个属性。`messageKey` 为 i18n 资源文件的键名，不直接存储消息文本。

#### Scenario: 枚举值结构
- **WHEN** 访问 `ErrorCode.CREDENTIALS_INVALID`
- **THEN** `getCode()` 返回 2002，`getMessageKey()` 返回 `"auth.credentials_invalid"`

#### Scenario: 根据 code 查找枚举
- **WHEN** 调用 `ErrorCode.fromCode(2002)`
- **THEN** 返回 `ErrorCode.CREDENTIALS_INVALID`

### Requirement: 必需错误码定义
`ErrorCode` 枚举 SHALL 至少包含以下错误码：

- SUCCESS(0, "result.success")
- PARAM_MISSING(1001, "param.missing")
- PARAM_INVALID(1002, "param.invalid")
- PARAM_TYPE_ERROR(1003, "param.type_error")
- NOT_AUTHENTICATED(2001, "auth.not_authenticated")
- CREDENTIALS_INVALID(2002, "auth.credentials_invalid")
- ACCOUNT_DISABLED(2003, "auth.account_disabled")
- SESSION_EXPIRED(2004, "auth.session_expired")
- ACCESS_DENIED(2005, "auth.access_denied")
- INTERNAL_ERROR(5001, "system.internal_error")
- DATABASE_ERROR(5002, "system.database_error")
- REMOTE_CALL_ERROR(5003, "system.remote_call_error")
- UNKNOWN_ERROR(9999, "system.unknown_error")

#### Scenario: 所有必需错误码可用
- **WHEN** 业务逻辑需要返回任一标准错误
- **THEN** 对应的 ErrorCode 枚举值 SHALL 存在且可被引用
