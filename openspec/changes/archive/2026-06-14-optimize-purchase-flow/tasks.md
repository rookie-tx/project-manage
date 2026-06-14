## 1. 数据库变更

- [x] 1.1 在 `schema.sql` 中为 `t_purchase_order` 添加 `purchase_request_id BIGINT UNIQUE REFERENCES t_purchase_request(id)` 字段
- [x] 1.2 同步 `schema-mysql.sql` 中对应的字段变更和索引
- [x] 1.3 删除 `schema.sql` 和 `schema-mysql.sql` 中 `t_purchase_order_item` 建表语句及相关 setval
- [x] 1.4 清理 `PurchaseOrderMapper.xml`：移除 insertItem/deleteItemsByOrderId/selectRequestIdsByOrderId，LEFT JOIN t_purchase_order_item 改为直接 JOIN t_purchase_request
- [x] 1.5 清理 `PurchaseRequestMapper.xml`：existsInPurchaseOrder 和 selectAvailable 改用 t_purchase_order.purchase_request_id
- [x] 1.6 清理 Java 代码：移除 PurchaseOrderMapper/PurchaseOrderRepository/PurchaseOrderRepositoryImpl 中的 insertItem/selectRequestIds/findRequestIds/deleteItems 方法
- [x] 1.7 清理 `PurchaseOrderApplicationService`：create/update 移除 purchaseRequestIds 列表参数，toResponse 用 getPurchaseRequestId() 查询单个 PR
- [x] 1.8 清理 `CreatePurchaseOrderRequest` 和 `UpdatePurchaseOrderRequest`：purchaseRequestIds → purchaseRequestId
- [x] 1.9 新增 ErrorCode：`PO_DUPLICATE`、`PO_STATUS_INVALID`、`PO_SUPPLIER_REQUIRED`

## 2. BPMN 流程

- [x] 2.1 重写 `purchase-request-approval.bpmn20.xml` 为单节点（pmApproval → approved/rejected）
- [x] 2.2 `PurchaseRequestTaskCallback.validateBeforeStart` 移除财务/总经理校验，仅保留 pmUserId
- [x] 2.3 `PurchaseRequestTaskCallback.onTaskCompleted` 中调用 PO 自动创建服务

## 3. 后端 PO 模块

- [x] 3.1 `PurchaseOrder` 实体新增 `purchaseRequestId` 字段；修改 `create` 和 `fromPersistence` 工厂方法
- [x] 3.2 `PurchaseOrderPO` 增加 `purchaseRequestId`；`PurchaseOrderPoMapper` 映射更新
- [x] 3.3 `PurchaseOrderMapper` SQL 增加 `purchaseRequestId` 列；新增 `selectByPRId` 查询
- [x] 3.4 `CreatePurchaseOrderRequest` 简化为仅需 `purchaseRequestId`（供应商和价格可选）
- [x] 3.5 `PurchaseOrderApplicationService.create` 重写：从 PR 复制基本信息，供应商/价格留空
- [x] 3.6 `PurchaseOrderApplicationService.changeStatus` 增加 PENDING→PURCHASED 时校验供应商信息已填写
- [x] 3.7 `PurchaseOrderRepository` 新增 `findByStatus` 方法（支持 tab=pending 过滤 supplierName 是否为空）
- [x] 3.8 `PurchaseOrderController.list` 增加 `tab` 参数支持

## 4. 后端入库联动

- [x] 4.1 新增 `GET /api/purchase-orders/pending-inbound` 接口（返回 status=PURCHASED 的 PO 列表）
- [x] 4.2 `MaterialController.recordStock` 入口后增加：若关联 purchaseRequestId，自动调用 PO status → ARRIVED

## 5. Web 前端采购管理页

- [x] 5.1 `PurchaseOrderListPage.vue` 重构为 Tab 模式（待采购 / 采购中 / 已采购 / 已到货）
- [x] 5.2 待采购 Tab：列表展示 + 编辑 Modal（补填供应商名称、电话、单价、总金额、采购日期）
- [x] 5.3 采购中 Tab：列表中每条可点击"采购完成"流转到已采购
- [x] 5.4 已采购 Tab：列表中每条可点击"入库"弹出 Modal（选仓库、物料、数量）
- [x] 5.5 已到货 Tab：只读展示
- [x] 5.6 `stores/purchaseOrder.js` 适配新 API 参数

## 6. Web 前端入库管理页

- [x] 6.1 `StockRecordListPage.vue` 新增"待入库"区块展示 pending-inbound 列表
- [x] 6.2 入库 Modal 增加"选择采购记录"入口，选择后预填物料信息

## 7. 小程序采购管理

- [x] 7.1 `pages/purchase-order/list` 适配 Tab 切换（待采购 / 采购中 / 已采购 / 已到货）
- [x] 7.3 已采购 Tab 增加"入库"按钮
- [x] 7.4 `api/purchaseOrder.js` 适配新接口

## 8. i18n 与验证

- [x] 8.1 补充新增 ErrorCode 的中文消息（`messages.properties`）
- [x] 8.3 编译验证通过
