## 1. 采购申请编号

- [x] 1.1 `schema.sql` — `t_purchase_request` 加 `code VARCHAR(30)` 列
- [x] 1.2 `PurchaseRequest.java` + `PurchaseRequestPO.java` — 实体加 code 字段
- [x] 1.3 `PurchaseRequestResponse.java` — 响应加 code 字段
- [x] 1.4 `PurchaseRequestApplicationService.java` — 新增 `generateCode()` 方法
- [x] 1.5 `PurchaseRequestController.java` — 创建时调用 `generateCode()`
- [x] 1.6 `PurchaseRequestMapper.xml` — INSERT/UPDATE 加 code 列

## 2. 中间表 + 1:N 关联

- [x] 2.1 `schema.sql` — 创建 `t_purchase_order_item` 表，迁移旧数据，删除 `t_purchase_order.purchase_request_id` 列
- [x] 2.2 `PurchaseOrder.java` — 去掉 `purchaseRequestId`，改存 `List<Long> purchaseRequestIds`
- [x] 2.3 `CreatePurchaseOrderRequest.java` — `purchaseRequestId` → `purchaseRequestIds` (List)
- [x] 2.4 `PurchaseOrderMapper.xml` — 调整查询，关联中间表
- [x] 2.5 `PurchaseOrderApplicationService.java` — 创建/更新时写中间表，查询时 JOIN
- [x] 2.6 `PurchaseOrderController.java` — 适配新参数
- [x] 2.7 `PurchaseOrderResponse.java` — 关联多个申请信息

## 3. available 端点

- [x] 3.1 `PurchaseRequestMapper.xml` — 新增 `selectAvailable` 查询
- [x] 3.2 `PurchaseRequestController.java` — 新增 `GET /api/purchase-requests/available`

## 4. Web SPA 适配

- [x] 4.1 `PurchaseOrderFormPage.vue` — 单选→多选，展示项目/物料/规格/数量/单位/到场时间
- [x] 4.2 `PurchaseOrderListPage.vue` + `PurchaseOrderDetailPage.vue` — 展示关联的多个申请

## 5. 小程序适配

- [x] 5.1 `purchase-order/form.*` — 多选 + 详情展示
- [x] 5.2 `purchase-order/list.*` + `purchase-order/detail.*` — 展示关联的多个申请
