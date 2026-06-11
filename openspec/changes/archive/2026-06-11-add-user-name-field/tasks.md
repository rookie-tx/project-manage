## 1. 数据库

- [x] 1.1 `schema.sql` 新增 `ALTER TABLE t_user ADD COLUMN IF NOT EXISTS name VARCHAR(50)` DDL

## 2. User 模块持久层 & 领域层

- [x] 2.1 `UserPO.java` 新增 `name` 字段
- [x] 2.2 `UserMapper.xml` 所有 SELECT 查询加 `name` 列
- [x] 2.3 `User.java` 领域实体新增 `name` 字段，更新 `create()` 和 `fromPersistence()` 工厂方法
- [x] 2.4 `UserDomainService.createUser()` 新增 `name` 参数

## 3. 认证链路（LoginContext / JWT / Session）

- [x] 3.1 `LoginContext` record 新增 `name` 字段，更新简洁构造函数
- [x] 3.2 `LoginContextHolder` 新增 `getName()` 静态方法（name 为 null 时回退 getUsername()）
- [x] 3.3 `JwtTokenProvider.createToken()` 将 name 写入 JWT claims，`parseToken()` 解析时提取 name
- [x] 3.4 `AuthenticationInterceptor` enrich LoginContext 时查询 t_user.name 并写入
- [x] 3.5 `LoginApplicationService.login()` 构造 LoginContext 时传入 name（从 AuthenticationResult 获取）
- [x] 3.6 `WechatAuthController.buildLoginContext()` 构造 LoginContext 时传入 name

## 4. API 响应 DTO

- [x] 4.1 `LoginResponse` 新增 `name` 字段
- [x] 4.2 `UserResponse` (user 模块) 新增 `name` 字段
- [x] 4.3 `CreateUserRequest` 新增 `name` 字段（可选）
- [x] 4.4 用户 Controller 创建/更新接口支持 `name` 字段

## 5. TempWorker 模块

- [x] 5.1 `CreateTempWorkerRequest` 新增 `@NotBlank String name` 字段
- [x] 5.2 `UpdateTempWorkerRequest` 新增 `String name` 字段（可选）
- [x] 5.3 `TempWorker.java` 领域实体新增 `name` 字段，更新 `fromPersistence()` 工厂方法
- [x] 5.4 `TempWorkerPO.java` 新增 `name` 字段
- [x] 5.5 `TempWorkerMapper.xml` 所有 SELECT 查询加 `u.name`
- [x] 5.6 `TempWorkerDomainService.create()` 新增 `name` 参数，传递给 `userDomainService.createUser()`
- [x] 5.7 `TempWorkerController.create()` 传递 `request.getName()` 给 domainService

## 6. 审批流程

- [x] 6.1 `TaskApplicationService.completeTask()` 中 `assigneeName` 改用 `LoginContextHolder.getName()`

## 7. 前端

- [x] 7.1 `TempWorkerTab.vue` 创建表单新增"姓名"输入框（必填），列表"姓名"列显示 `name || username`
- [x] 7.2 `DashboardLayout.vue` Header 头像旁显示 `auth.user?.name || auth.user?.username`
- [x] 7.3 用户管理列表页新增"姓名"列
- [x] 7.4 用户编辑页新增"姓名"输入框
