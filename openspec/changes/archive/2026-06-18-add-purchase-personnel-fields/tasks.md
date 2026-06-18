## 1. 数据库变更

- [x] 1.1 在 `schema-mysql.sql` 的 `t_purchase_order` 表定义中新增 `purchaser_id BIGINT COMMENT '采购人ID'` 和 `inbound_operator_id BIGINT COMMENT '入库人ID'` 列
- [x] 1.2 在 `schema.sql`（PostgreSQL）的 `t_purchase_order` 表定义中同步新增两列

## 2. 后端领域层

- [x] 2.1 `PurchaseOrder.java` 领域实体新增 `purchaserId` 和 `inboundOperatorId` 字段，更新工厂方法 `createAuto()`、`create()`、`fromPersistence()` 的参数和赋值
- [x] 2.2 `PurchaseOrderPO.java` 持久化对象新增 `purchaserId` 和 `inboundOperatorId` 字段
- [x] 2.3 `PurchaseOrderMapper.xml` 更新 `insert`、`selectById`、`selectByPage` 等 SQL，包含新字段
- [x] 2.4 `PurchaseOrderMapper.java` 接口新增 `updatePurchaser` 和 `updateInboundOperator` 方法（或扩展已有的 update 方法）

## 3. 后端应用层

- [x] 3.1 `PurchaseOrderApplicationService.changeStatus()` 在 PURCHASING→PURCHASED 时，从 `LoginContextHolder` 获取当前用户 ID 并设置到 PO 的 `purchaserId`，通过 repository 持久化
- [x] 3.2 `PurchaseOrderApplicationService.toResponse()` 中通过 `UserRepository.findById()` 解析 `purchaserName` 和 `inboundOperatorName`，设置到 Response DTO

## 4. 后端 API 层

- [x] 4.1 `PurchaseOrderResponse.java` 新增 `purchaserId`、`purchaserName`、`inboundOperatorId`、`inboundOperatorName` 四个字段
- [x] 4.2 `MaterialController.recordStock()` 在入库操作将 PO 状态更新为 ARRIVED 的同时，将当前用户 ID 写入 PO 的 `inbound_operator_id`

## 5. Web 前端

- [x] 5.1 `usePurchaseOrderList.js` 表格列定义中新增「采购人」列（映射 `purchaserName`）和「入库人」列（映射 `inboundOperatorName`），未记录时显示「-」
- [x] 5.2 确认 `PendingPurchasePage.vue` 采购完成弹窗提交后采购人自动记录（后端已处理，前端无需额外改动，验证流程即可）
- [x] 5.3 确认 `PurchasedPurchasePage.vue` 入库弹窗提交后入库人自动记录（后端已处理，前端无需额外改动，验证流程即可）
- [x] 5.4 `PurchaseOrderSummaryPage.vue` 表格展示采购人和入库人列

## 6. 小程序

- [x] 6.1 `list-behavior.js` 数据映射逻辑中处理 `purchaserName` 和 `inboundOperatorName` 字段的展平
- [x] 6.2 `purchased.wxml` 等列表页 WXML 模板新增采购人和入库人展示
- [x] 6.3 `detail.wxml` 详情页将原有的「采购员」（映射 `createdByName`）改为「创建人」，新增独立的「采购人」和「入库人」展示字段
