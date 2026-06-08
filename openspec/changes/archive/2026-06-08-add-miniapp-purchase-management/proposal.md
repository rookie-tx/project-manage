## Why

后端已具备完整的采购申请管理 API（CRUD + 提交审批 + 审批历史），Web SPA 也实现了采购申请的完整功能（列表、新建、详情、编辑、提交审批），但小程序端完全没有采购申请功能。采购申请是工地现场人员的核心需求——通过手机填写采购需求、跟踪审批进度，无需回到办公室用电脑操作。

## What Changes

- **小程序新增**：3 个采购申请页面——列表页（项目筛选 + 状态驱动的操作按钮 + 分页）、表单页（新建/编辑复用，通过 `id` 参数区分模式）、详情页（字段展示 + 审批人选 + 提交审批 + 审批历史时间线）
- **小程序修改**：审批操作页 `pages/approval/action` 增加"查看采购详情"链接，当 `businessType` 为 `PURCHASE_REQUEST` 时显示
- **小程序修改**：Dashboard 新增 📦 采购申请功能入口（第 5 个入口，Flex 布局自动适配）
- 后端无需改动，所有 API 已就绪

## Capabilities

### New Capabilities
- `miniapp-purchase-management`: 小程序采购申请管理。包含列表页（按项目筛选、按状态显示不同操作按钮、上下拉分页）、新建/编辑复用表单页（项目选择器、单位枚举选择器）、详情页（含审批人选、提交审批嵌入区、审批历史时间线）、审批页采购详情跳转链接。

### Modified Capabilities
- `miniapp-approval`: 审批操作页新增"查看采购详情"跳转链接——当任务的 `businessType` 为 `PURCHASE_REQUEST` 时，显示可点击的链接跳转到采购申请详情页。

## Impact

- 后端：无改动
- 小程序：新建 `api/purchase.js` + 12 个页面文件（list/form/detail 各 4 个）；修改 `pages/approval/action.*`（wxml+js）、`dashboard.*`（js+wxml）、`app.json`
- 无破坏性变更
