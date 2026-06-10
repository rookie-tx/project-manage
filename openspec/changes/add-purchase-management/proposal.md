## Why

采购申请审批通过后，采购部需要实际去市场上执行采购。当前系统在审批通过后没有后续流程——采购员无法记录供应商信息、实际价格、合同附件等采购执行数据。需要新增"采购管理"模块，作为采购申请和仓库入库之间的桥梁。

## What Changes

- **后端新增**：`purchasemanagement` 模块（DDD 四层），提供采购记录 CRUD API + 文件上传关联
- **数据库新增**：`t_purchase_order` 表（关联 purchase_request_id UNIQUE，一对一）
- **Web SPA 新增**：3 个页面——列表、新建/编辑表单、详情（含合同附件展示）
- **小程序新增**：3 个页面——列表、表单、详情，Dashboard 新增 📋 入口（第 6 个）
- 文件上传复用已有 `t_upload_file` 机制

## Capabilities

### New Capabilities
- `purchase-order-api`: 采购记录后端 API——CRUD + 文件关联 + 状态流转
- `purchase-order-ui`: Web SPA 采购管理页面
- `miniapp-purchase-order`: 小程序采购管理页面

### Modified Capabilities
<!-- None -->

## Impact

- 后端：新模块 ~12 文件
- 数据库：1 新表 `t_purchase_order`
- Web SPA：~5 文件（3 页面 + store + router + 导航菜单）
- 小程序：~14 文件（API + 3 页面 × 4 + dashboard + app.json）
- 无破坏性变更
