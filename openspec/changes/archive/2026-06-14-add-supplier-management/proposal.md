## Why

当前系统采购管理模块中的供应商信息以纯文本形式存在于采购记录中，无法对供应商进行系统性管理。同时，微信小程序缺失供应商自助入驻入口，供应商无法通过小程序注册并参与平台业务。本次变更建立供应商管理基础设施，支持供应商通过小程序自助注册，为后续供应商接单功能奠定基础。

## What Changes

- **新增供应商领域模块**：后端建立 `supplier` DDD 四层模块，包含供应商实体、仓储、领域服务、应用服务和管理 API
- **新增供应商数据库表**：`t_supplier` 表，与用户一对一关联，包含企业名称、统一社会信用代码、联系人、联系电话、地址等信息
- **小程序供应商注册**：新增注册页面，用户通过微信授权后填写企业信息，一步完成账号创建、微信绑定和供应商资料录入，无需审批
- **Web 端供应商管理**：新增供应商列表页和详情/编辑页，支持管理员对供应商进行 CRUD 操作
- **新增供应商角色与权限**：预置 `supplier` 角色及相关权限，注册时自动分配
- **采购订单不变更**：本次不修改 `t_purchase_order` 表，供应商字段保持纯文本

## Capabilities

### New Capabilities

- `supplier-management`: Web 管理端供应商 CRUD（列表、创建、编辑、删除），支持分页和搜索
- `supplier-registration`: 小程序供应商注册流程，微信授权后填写企业信息一键注册
- `supplier-profile`: 小程序供应商查看和编辑自己的企业资料

### Modified Capabilities

<!-- 本次不修改现有 capability -->

## Impact

| 范围 | 影响 |
|------|------|
| 数据库 | 新增 `t_supplier` 表；种子数据新增 `supplier` 角色和 6 条权限 |
| 后端 | 新增 `cn.tx.aiagent.supplier` 包（~12 个文件）；`WechatAuthController` 无需修改（注册独立接口） |
| Web 前端 | 新增供应商管理路由和 2 个页面；`stores/supplier.js` |
| 微信小程序 | 新增 `pages/supplier/` 注册页和个人资料页；`api/supplier.js`；登录页增加注册入口 |
| API | 新增 `POST /api/supplier/register`（公开）、`GET/POST/PUT/DELETE /api/suppliers`（需认证+权限） |
