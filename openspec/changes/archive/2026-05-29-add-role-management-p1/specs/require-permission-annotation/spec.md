## ADDED Requirements

### Requirement: @RequirePermission 注解声明
系统 SHALL 提供 `@RequirePermission` 注解，用于标记 Controller 方法，声明调用该接口所需的权限。

#### Scenario: 标记方法
- **WHEN** 开发者在 Controller 方法上添加 `@RequirePermission("user:delete")`
- **THEN** 注解编译通过，运行时 AOP 切面拦截该方法调用

### Requirement: 权限校验通过
系统 SHALL 在当前用户拥有所需权限时允许方法正常执行。

#### Scenario: 用户拥有权限
- **WHEN** 当前用户 permissions 包含 `user:delete`，调用标注 `@RequirePermission("user:delete")` 的方法
- **THEN** 方法正常执行，返回业务结果

### Requirement: 权限校验拒绝
系统 SHALL 在当前用户缺少所需权限时抛出 `BusinessException(ACCESS_DENIED)`。

#### Scenario: 用户缺少权限
- **WHEN** 当前用户 permissions 不包含 `user:delete`，调用标注 `@RequirePermission("user:delete")` 的方法
- **THEN** 切面拦截并抛出 `BusinessException(ErrorCode.ACCESS_DENIED)`，HTTP 响应 `code: 2005`

### Requirement: 未登录用户
系统 SHALL 在未登录用户调用受保护方法时返回未认证错误。

#### Scenario: 未登录调用
- **WHEN** 未登录用户（LoginContext 为 null）调用受保护方法
- **THEN** 切面抛出 `BusinessException(NOT_AUTHENTICATED)`

### Requirement: 未标注方法不受影响
系统 SHALL 不拦截未标注 `@RequirePermission` 的 Controller 方法。

#### Scenario: 未标注方法正常执行
- **WHEN** Controller 方法未标注 `@RequirePermission`
- **THEN** 切面直接放行，方法正常执行
