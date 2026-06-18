## Why

当前采购流程中，审批通过后由管理员直接在采购单中手动填写供应商信息，缺乏供应商参与报价的环节。需要引入报价管理机制，让已注册的供应商能够对审批通过的采购物料进行报价，管理员可在待采购详情页查看所有报价并选用。

## What Changes

- **新增报价实体 (Quotation)**：供应商对采购单提交的报价，包含单价、总金额、预计交货日期、备注等字段
- **新增供应商端报价功能（小程序）**：供应商登录后可查看待报价物料列表、提交报价、查看自己的报价历史
- **新增管理端报价展示（Web）**：待采购详情页展示该采购单收到的所有供应商报价
- **新增报价选用机制**：管理员可选用某条报价，自动将供应商信息、价格等填入采购单，完成采购闭环
- **采购单 API 扩展**：增加获取报价列表、选用报价的接口端点

## Capabilities

### New Capabilities

- `quotation-management`: 报价管理核心后端，包含报价实体、领域服务、CRUD API、报价选用逻辑
- `supplier-quotation`: 供应商端报价功能（小程序），包含待报价列表、提交报价、我的报价页面和 API
- `purchase-quotation-view`: 管理端报价查看功能（Web），在待采购详情页展示报价列表，支持选用报价操作

### Modified Capabilities

- `purchase-order-api`: 采购单 API 扩展，增加 `GET /api/purchase-orders/{id}/quotations` 和 `POST /api/purchase-orders/{id}/quotations/{quotationId}/select` 端点
- `purchase-order-ui`: 采购单详情页增加报价列表展示区域和选用报价交互

## Impact

- **后端新增模块**：`quotation/` 包（DDD 四层），新增 `t_quotation` 数据库表
- **前端 Web**：`PurchaseOrderFormPage.vue` 或待采购详情页新增报价展示区域
- **小程序**：新增报价相关页面（待报价列表、报价表单、我的报价）、新增 `api/quotation.js` API 模块、首页 Dashboard 新增入口
- **权限**：供应商角色需能访问报价相关接口；无需新增权限码
- **不涉及**：现有采购申请审批流程、采购单状态流转逻辑、供应商注册流程均保持不变
