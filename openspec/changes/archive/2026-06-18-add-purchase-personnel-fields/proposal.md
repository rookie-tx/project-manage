## Why

采购单目前只有 `createdBy`（创建人）字段，缺少对采购执行人和入库操作人的明确记录。创建人不等于采购人（采购申请提交者可能不是实际执行采购的人），且入库操作完成后没有在采购单上留痕。需要在采购单上独立记录采购人和入库人，便于后续追溯和责任认定。

## What Changes

- 在 `t_purchase_order` 表新增 `purchaser_id`（采购人ID）和 `inbound_operator_id`（入库人ID）两个字段
- 采购完成时（状态从「采购中」变为「已采购」），自动将当前登录用户记录为采购人
- 入库时（状态从「已采购」变为「已到货」），自动将当前登录用户记录为入库人
- API 响应中新增 `purchaserId`、`purchaserName`、`inboundOperatorId`、`inboundOperatorName` 字段，展示用户名
- Web 端采购单列表和详情增加采购人、入库人两列
- 小程序采购单列表和详情增加采购人、入库人展示

## Capabilities

### New Capabilities

- `purchase-personnel-tracking`: 采购单人员追踪 — 在采购单上独立记录采购人和入库人，采购完成和入库操作时自动赋值，列表和详情展示用户名

### Modified Capabilities

- `purchase-order-api`: API 响应新增 `purchaserId`、`purchaserName`、`inboundOperatorId`、`inboundOperatorName` 字段；`changeStatus` 接口在采购完成时记录采购人
- `purchase-order-ui`: Web 端表格新增采购人列和入库人列
- `miniapp-purchase-order`: 小程序列表和详情新增采购人、入库人展示
- `purchase-inbound`: 入库流程在变更采购单状态为已到货时，同步记录入库人

## Impact

- **数据库**: `t_purchase_order` 表新增两列（MySQL/PostgreSQL schema）
- **后端**: `PurchaseOrder` 领域实体、PO、Mapper XML、Response DTO、ApplicationService、MaterialController
- **Web 前端**: `usePurchaseOrderList.js` 表格列定义、4 个采购单页面组件
- **小程序**: `list-behavior.js` 数据映射、列表和详情 WXML
