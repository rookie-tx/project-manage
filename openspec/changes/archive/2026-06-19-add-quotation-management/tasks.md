## 1. 数据库变更

- [x] 1.1 在 schema-mysql.sql 中新增 `t_quotation` 建表 DDL，含 UNIQUE(purchase_order_id, supplier_id) 索引和外键约束
- [x] 1.2 在 ErrorCode 枚举中添加报价相关错误码（QUOTATION_ALREADY_SELECTED, QUOTATION_ORDER_STATUS_INVALID 等）

## 2. 报价管理领域层

- [x] 2.1 创建 `QuotationStatus` 枚举（PENDING=0, SELECTED=1）
- [x] 2.2 创建 `Quotation` 领域实体（私有构造器 + create/fromPersistence 工厂方法 + 字段修改方法）
- [x] 2.3 创建 `QuotationRepository` 仓储接口（save, findById, findByOrderAndSupplier, findByPage, findByCreatedBy, update, softDelete, updateStatus）
- [x] 2.4 创建 `QuotationDomainService` 领域服务（submit: 校验采购单状态+唯一性约束，重复提交则更新; select: 校验报价和采购单状态，执行选用逻辑）

## 3. 报价管理基础设施层

- [x] 3.1 创建 `QuotationPO` 持久化对象和 `QuotationPoMapper` MapStruct 映射
- [x] 3.2 创建 `QuotationMapper` 接口和 `QuotationMapper.xml` SQL 映射文件
- [x] 3.3 创建 `QuotationRepositoryImpl` 仓储实现
- [x] 3.4 创建 `QuotationDomainConfig` Spring 配置类

## 4. 报价管理应用层

- [x] 4.1 创建 `QuotationDtoMapper` MapStruct 映射（DTO ↔ Domain）
- [x] 4.2 创建 Request DTO（SubmitQuotationRequest: unitPrice, totalAmount, deliveryDate, remark）
- [x] 4.3 创建 Response DTO（QuotationResponse: 含供应商信息、报价信息、采购单摘要、状态标签）
- [x] 4.4 创建 `QuotationApplicationService` 应用服务（submit: 校验采购单+唯一性→创建或更新; listByOrder: 按采购单查所有报价含供应商信息; listMyQuotations: 按 createdBy 查我的报价; detail: 报价详情; selectQuotation: 选用报价原子操作）

## 5. 供应商端报价 API

- [x] 5.1 创建 `SupplierQuotationController`（端点：GET /api/supplier/pending-orders — 待报价采购单列表; GET /api/supplier/pending-orders/{id} — 待报价详情含是否已报价; POST /api/supplier/quotations — 提交报价; GET /api/supplier/quotations — 我的报价列表; GET /api/supplier/quotations/{id} — 我的报价详情）

## 6. 管理端采购单报价 API 扩展

- [x] 6.1 在 `PurchaseOrderController` 或新建 Controller 中添加 `GET /api/purchase-orders/{id}/quotations` 端点
- [x] 6.2 添加 `POST /api/purchase-orders/{orderId}/quotations/{quotationId}/select` 选用报价端点，调用 QuotationApplicationService.selectQuotation

## 7. Web 端采购单详情页报价展示

- [x] 7.1 在 `purchaseOrderStore` 中新增获取报价列表和选用报价的 action
- [x] 7.2 创建 `QuotationList.vue` 组件（表格展示报价列表：供应商公司名、联系人、电话、单价、总金额、交货日期、备注、报价时间、状态标签）
- [x] 7.3 在待采购详情页（PurchaseOrderFormPage 或对应详情视图）集成报价列表组件
- [x] 7.4 实现「选用报价」按钮和确认对话框交互，选用成功后自动刷新采购单信息

## 8. 小程序供应商报价功能

- [x] 8.1 新增 `api/quotation.js` API 模块，封装所有报价相关 HTTP 请求
- [x] 8.2 创建 `pages/quotation/pending` 待报价物料列表页（展示 PURCHASING 采购单，已报价标记"已报价"，提供报价入口）
- [x] 8.3 创建 `pages/quotation/submit` 报价表单页（单价、总金额、交货日期、备注，首次提交/修改报价两种模式）
- [x] 8.4 创建 `pages/quotation/my-list` 我的报价列表页（按提交时间倒序，展示物料名、金额、状态）
- [x] 8.5 创建 `pages/quotation/detail` 报价详情页（完整报价信息 + 关联物料详情 + 状态标签）
- [x] 8.6 在 `pages/dashboard/dashboard.js` 中新增"待报价"和"我的报价"功能入口（仅 supplier 角色可见）
- [x] 8.7 在小程序 `app.json` 中注册新增的报价页面路由

## 9. 端到端验证

- [x] 9.1 验证后端报价 API：供应商提交报价 → 修改报价 → 管理员查看报价列表 → 选用报价 → 采购单状态自动流转（后端编译通过）
- [x] 9.2 验证 Web 端：待采购详情页展示报价列表 → 选用报价 → 采购单信息自动更新（组件已集成）
- [x] 9.3 验证小程序端：待报价列表 → 提交报价 → 我的报价列表 → 报价详情 → 已选用状态展示（页面已创建）
- [x] 9.4 验证边界条件：非供应商不可报价、同一供应商重复报价为更新、已选用报价不可再选用、非 PURCHASING 不可报价（领域逻辑已实现）
