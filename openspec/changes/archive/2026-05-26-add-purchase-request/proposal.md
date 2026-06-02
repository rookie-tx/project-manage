## Why

采购系统是项目管理的下游业务模块，项目部需要提交采购申请来请求物料。当前系统完全没有采购申请功能，项目部无法在线提交和管理采购需求。本次新增采购申请模块作为采购链的起点，后续采购部、仓管员等角色将在此基础上操作。

## What Changes

### 后端
- 新建 `cn.tx.aiagent.purchaserequest` 模块，遵循 DDD 四层架构
- `POST /api/purchase-requests` — 创建采购申请
- `GET /api/purchase-requests` — 列表查询（按项目筛选 + 分页）
- `GET /api/purchase-requests/{id}` — 详情查询
- `PUT /api/purchase-requests/{id}` — 更新采购申请
- `DELETE /api/purchase-requests/{id}` — 软删除
- 新建 `t_purchase_request` 数据库表
- `ProjectSelect` 复用已有项目管理模块，新增 `GET /api/projects/select` 接口供下拉选择

### 前端
- 新建 `stores/purchaseRequest.js` — 采购申请 Pinia store
- 新建 `views/purchase/PurchaseRequestListPage.vue` — 列表页（按项目筛选 + 分页）
- 新建 `views/purchase/PurchaseRequestCreatePage.vue` — 新建页
- 新建 `views/purchase/PurchaseRequestDetailPage.vue` — 详情页
- 新建 `views/purchase/PurchaseRequestEditPage.vue` — 编辑页（含状态流转）
- 新建 `components/PurchaseRequestForm.vue` — 表单复用组件
- 新建 `components/ProjectSelect.vue` — 项目下拉选择器
- 修改 `router/index.js` — 新增 4 条路由
- 修改 `views/dashboard/DashboardLayout.vue` — 新增"采购申请"菜单项

## Capabilities

### New Capabilities
- `purchase-request-api`: 后端采购申请 CRUD API，包括创建、列表查询（按项目筛选+分页）、详情、更新（含状态流转）、软删除
- `purchase-request-ui`: 前端采购申请管理界面，包括列表页、新建页、详情页、编辑页，以及项目下拉选择器组件
- `project-select-api`: 后端新增 `GET /api/projects/select` 接口，返回项目列表（仅 id + code + name）供前端下拉选择

### Modified Capabilities
- 无

## Impact

### 后端新增文件
- `ai-agent-server/src/main/java/cn/tx/aiagent/purchaserequest/` 包下完整的 DDD 四层（约 18 个文件）
- `ai-agent-server/src/main/resources/mapper/PurchaseRequestMapper.xml`
- `ai-agent-server/src/main/resources/db/schema.sql` — 新增 t_purchase_request 建表语句

### 后端修改文件
- `ai-agent-server/src/main/java/cn/tx/aiagent/project/` — 新增 `GET /api/projects/select` 接口

### 前端新增文件
- `ai-agent-client/src/stores/purchaseRequest.js`
- `ai-agent-client/src/views/purchase/` 下 4 个页面
- `ai-agent-client/src/components/PurchaseRequestForm.vue`
- `ai-agent-client/src/components/ProjectSelect.vue`

### 前端修改文件
- `ai-agent-client/src/router/index.js`
- `ai-agent-client/src/views/dashboard/DashboardLayout.vue`
