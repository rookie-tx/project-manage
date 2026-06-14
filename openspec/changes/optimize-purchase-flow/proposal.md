## Why

当前采购审批需要三级流转（部门经理 → 财务 → 总经理），实际业务中仅项目经理即可完成审批决策。采购申请审批通过后需手动创建采购记录，存在重复录入和漏建风险。同时入库管理与采购管理割裂，采购完成后无法自动衔接入库流程。

## What Changes

- **BPMN 简化**：采购申请审批流程从 3 节点压缩为单节点（项目经理），移除财务和总经理审批环节
- **PR : PO 一对一**：`t_purchase_order` 新增 `purchase_request_id` UNIQUE FK，**删除** `t_purchase_order_item` 表及其所有相关代码。**BREAKING**: 移除多对多关联
- **审批通过自动创建 PO**：PR 审批通过后自动创建 PO 记录（状态=采购中），PR 中的名称、规格、品牌、数量、单位、到货日期、项目 ID 带入 PO；供应商名称/电话、单价、总金额留空
- **采购管理页重构**：新增 Tab "待采购"展示待填信息的 PO；采购人编辑补充供应商和价格后流转为"采购中"；点击"采购完成"流转为"已采购"
- **入库联动**：已采购状态的 PO 自动出现在入库管理页；点击入库选择仓库和物料后创建入库记录，PO 流转为"已到货"
- **小程序采购管理同步更新**：采购管理页适配新的 1:1 模式和入库联动

## Capabilities

### New Capabilities

- `purchase-inbound`: 采购完成后的入库联动，入库管理页展示已采购 PO，入库操作创建 StockRecord 并流转 PO 状态为已到货

### Modified Capabilities

- `purchase-request-api`: BPMN 审批流程从 3 节点简化为单节点（项目经理），移除 financialManagerId 和 generalManagerId 变量
- `purchase-order-api`: PR 与 PO 改为一对一关系，审批通过自动创建，PO 创建接口改为仅接受 purchaseRequestId
- `purchase-order-ui`: Web 端采购管理页增加 Tab 切换（待采购 / 采购中 / 已采购 / 已到货），待采购 Tab 支持编辑供应商信息
- `approval-task-management`: BPMN 流程定义文件替换，审批路由从逐级审批改为单节点

## Impact

| 范围 | 影响 |
|------|------|
| 数据库 | `t_purchase_order` 新增 `purchase_request_id` UNIQUE FK；**删除** `t_purchase_order_item` 表及 setval |
| BPMN | `purchase-request-approval.bpmn20.xml` 重写为单节点 |
| 后端 PR 模块 | `PurchaseRequestTaskCallback` 移除财务/总经理校验，`onProcessStarted` → 触发创建 PO；`PurchaseRequestMapper.xml` 移除 t_purchase_order_item 子查询 |
| 后端 PO 模块 | `PurchaseOrder` 实体加 `purchaseRequestId`；`CreatePurchaseOrderRequest` 简化为仅需 `purchaseRequestId`；移除 `insertItem`/`deleteItemsByOrderId`/`findRequestIdsByOrderId` 方法；新增入库联动 API |
| 后端仓库模块 | `StockRecordController` 或 `MaterialController` 新增"待入库列表"查询接口 |
| Web 前端 | `PurchaseOrderListPage.vue` 重构为 Tab 模式；`StockRecordListPage.vue` 新增加入库联动入口 |
| 小程序 | `pages/purchase-order/` 适配 1:1 模式和入库联动 |
