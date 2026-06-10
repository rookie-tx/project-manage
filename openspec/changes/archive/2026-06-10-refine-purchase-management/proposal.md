## Why

采购管理模块上线后发现三个设计缺陷：采购申请缺少唯一编号不便于追溯；一条采购记录只能关联一个申请，但实际采购中常有多申请合并采购的场景；选择申请时信息不足，无法判断该选哪个。需要一次性修复这三个问题。

## What Changes

- **采购申请编号**：`t_purchase_request` 新增 `code` 字段（格式 `PRQ-年份-序号`），创建时自动生成
- **1:1 → 1:N**：新增中间表 `t_purchase_order_item`，去掉 `t_purchase_order.purchase_request_id` 及其 UNIQUE 约束；一条采购记录可关联多个申请，但一个申请只能被关联一次（`purchase_request_id UNIQUE`）
- **可选申请接口**：新增 `GET /api/purchase-requests/available` 端点，返回已通过且未被关联的申请，包含项目名、物料名、规格、数量、单位、到场时间
- 前端多选改为展示详情卡片

## Capabilities

### New Capabilities
<!-- All changes are modifications to existing modules -->

### Modified Capabilities
- `purchase-order-api`: 采购记录关联模型从 1:1 改为 1:N，新增中间表查询和 available 端点
- `purchase-request-api`: 新增 code 字段，新增 available 端点

## Impact

- 数据库：2 表变更（`t_purchase_request` +code，`t_purchase_order` -purchase_request_id）+ 1 新表
- 后端：~15 文件（purchaserequest + purchasemanagement 模块）
- Web SPA：~3 文件（表单 + 列表 + 详情）
- 小程序：~3 文件（表单 + 列表 + 详情）
- **BREAKING**：`t_purchase_order.purchase_request_id` 列移除，需迁移已有数据
