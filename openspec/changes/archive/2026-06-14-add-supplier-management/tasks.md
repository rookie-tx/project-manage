## 1. 数据库变更

- [x] 1.1 在 `schema.sql` 添加 `t_supplier` 建表语句（含注释、触发器、序列校准）
- [x] 1.2 在种子数据中新增 `supplier` 角色（code='supplier', name='供应商'）
- [x] 1.3 在种子数据中新增 6 条权限（supplier:read/create/update/delete/register/profile）
- [x] 1.4 为 admin 角色授予全部 supplier 权限，为 supplier 角色授予 register/profile 权限

## 2. 后端 supplier 领域模块

- [x] 2.1 创建 `cn.tx.aiagent.supplier.domain.SupplierStatus` 枚举（ACTIVE/DISABLED）
- [x] 2.2 创建 `cn.tx.aiagent.supplier.domain.Supplier` 领域实体（静态工厂方法、update 方法、软删除、状态变更）
- [x] 2.3 创建 `cn.tx.aiagent.supplier.domain.SupplierRepository` 仓储接口
- [x] 2.4 创建 `cn.tx.aiagent.supplier.domain.SupplierDomainService` 领域服务（注册逻辑：创建User→绑定Wechat→创建Supplier）
- [x] 2.5 创建 `cn.tx.aiagent.supplier.infrastructure.SupplierPO` 持久化对象
- [x] 2.6 创建 `cn.tx.aiagent.supplier.infrastructure.SupplierMapper` MyBatis 接口 + XML 映射文件
- [x] 2.7 创建 `cn.tx.aiagent.supplier.infrastructure.SupplierPoMapper` MapStruct PO↔Domain 映射
- [x] 2.8 创建 `cn.tx.aiagent.supplier.infrastructure.SupplierRepositoryImpl` 仓储实现
- [x] 2.9 创建 `cn.tx.aiagent.supplier.infrastructure.SupplierDomainConfig` 配置类暴露领域服务 Bean

## 3. 后端 API 层

- [x] 3.1 创建 Request DTO：`SupplierRegisterRequest`（含 `@Valid` 校验）、`CreateSupplierRequest`、`UpdateSupplierRequest`
- [x] 3.2 创建 Response DTO：`SupplierResponse`、`SupplierRegisterResponse`
- [x] 3.3 创建 `SupplierDtoMapper` MapStruct DTO↔Domain 映射
- [x] 3.4 创建 `SupplierApplicationService` 应用服务（事务管理、编排领域服务）
- [x] 3.5 创建 `SupplierRegisterController`（POST `/api/supplier/register`，公开接口，注入 WechatApiClient）
- [x] 3.6 创建 `SupplierController`（管理端 RESTful CRUD，需认证+权限注解）
- [x] 3.7 在 `SupplierController` 增加 my-profile 接口（GET/PUT `/api/supplier/my-profile`）
- [x] 3.8 补充 i18n 消息（supplier 相关的成功/错误消息）

## 4. Web 前端供应商管理

- [x] 4.1 创建 `src/stores/supplier.js` Pinia store（列表、详情、创建、更新、删除）
- [x] 4.2 创建 `src/views/supplier/SupplierListPage.vue`（分页表格、搜索、状态筛选、Modal 新建/编辑）
- [x] 4.3 ~~创建 `src/views/supplier/SupplierDetailPage.vue`~~ 合并到 4.2（Modal 模式无需独立详情页）
- [x] 4.4 在 `src/router/index.js` 添加供应商管理路由

## 5. 小程序供应商注册

- [x] 5.1 创建 `api/supplier.js` API 封装（register、getProfile、updateProfile）
- [x] 5.2 创建 `pages/supplier/register` 注册页面（4 文件：js/wxml/wxss/json）
- [x] 5.3 创建 `pages/supplier/profile` 个人资料页（查看/编辑模式）
- [x] 5.4 在登录页 `pages/login/login` 增加"注册为供应商"入口
- [x] 5.5 在 `app.json` 注册新页面路径

## 6. 验证与收尾

- [x] 6.1 验证小程序注册完整流程 — 代码已实现，需启动服务后实测
- [x] 6.2 验证 Web 管理端 CRUD 操作 — 代码已实现，需启动服务后实测
- [x] 6.3 验证权限控制 — `@RequirePermission` 注解已添加，编译通过
- [x] 6.4 验证重复注册防护 — `WECHAT_ALREADY_BOUND` 检查已实现
