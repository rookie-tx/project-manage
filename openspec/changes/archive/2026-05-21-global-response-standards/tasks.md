## 1. 核心类创建

- [x] 1.1 创建 `cn.tx.aiagent.common.api.Result<T>` 统一响应体，包含 `code`、`msg`、`data` 字段，提供 `success()`、`success(data)`、`error(code, msg)` 静态工厂方法
- [x] 1.2 创建 `cn.tx.aiagent.common.api.ErrorCode` 错误码枚举，包含 SUCCESS 及所有 1xxx/2xxx/5xxx/9xxx 错误码，每个枚举值持有 `code` 和 `messageKey`，并提供 `fromCode(int)` 静态查找方法
- [x] 1.3 创建 `cn.tx.aiagent.common.exception.BusinessException` 业务异常类，继承 `RuntimeException`，持有 `ErrorCode` 和可选的 `Object... args`

## 2. 国际化配置

- [x] 2.1 创建 `i18n/messages.properties`，包含所有错误码的中文消息
- [x] 2.2 创建 `i18n/messages_en_US.properties`，包含所有错误码的英文消息
- [x] 2.3 在 `application.yml` 中配置 `spring.messages.basename=i18n/messages`、`spring.messages.encoding=UTF-8`、`spring.web.locale=zh_CN`

## 3. 全局异常处理

- [x] 3.1 在 `cn.tx.aiagent.common.api` 中创建 `GlobalExceptionHandler`，处理 `BusinessException`、`MethodArgumentNotValidException`、`Exception`，统一返回 `Result`，通过 `MessageSource` 解析国际化消息
- [x] 3.2 删除旧的 `login.api.GlobalExceptionHandler`

## 4. Controller 改造

- [x] 4.1 修改 `LoginController`：返回类型改为 `Result<LoginResponse>`，错误分支用 `throw new BusinessException(ErrorCode.xxx)` 替代手动 `ResponseEntity`
- [x] 4.2 修改 `SessionController`：返回类型改为 `Result<LoginResponse>` 和 `Result<Void>`，未登录等错误分支用 `BusinessException` 替代

## 5. 验证

- [x] 5.1 编译通过，手动启动应用验证成功登录返回 `{ code:0, msg:"操作成功", data:{...} }`
- [x] 5.2 手动启动应用验证错误场景（错误密码）返回 `{ code:2002, msg:"用户名或密码错误", data:null }`
- [x] 5.3 手动启动应用验证参数校验（空用户名）返回 `{ code:1002, msg:"参数校验失败：...", data:null }`
- [x] 5.4 手动启动应用切换浏览器语言为英文，验证错误消息变为英文
