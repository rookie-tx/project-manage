## Why

当前 Web 端和小程序端的"采购管理"页面将待采购、已采购、已到货三个状态合并在一个页面内通过 Tab 切换，所有 Tab 共用同一个权限码 `purchase:read`，无法按状态维度做精细化权限控制。拆分后每个状态独立为一个菜单/入口，可分别授予不同角色（采购员看"待采购"，库管员看"已采购"做入库，管理人员看"采购汇总"全览），为后续权限管理打基础。

## What Changes

### Web 端（ai-agent-client）

- **拆分菜单**：将原来 1 个"采购管理"菜单拆为 4 个独立菜单——采购汇总、待采购、已采购、已到货
- **独立路由**：每个菜单对应独立路由，各自绑定独立权限码
- **内联操作**：待采购页面使用内联 Modal 完成"采购完成"操作，已采购页面使用内联 Modal 完成"入库"操作
- **移除手动新建**：删除 `/purchase-orders/new` 路由，采购单改为由采购申请审批通过后由后端自动创建
- **保留详情和编辑**：`/purchase-orders/:id` 详情页和 `/purchase-orders/:id/edit` 编辑页保持不变

### 小程序端（ai-agent-miniapp）

- **拆分功能入口**：将原来 1 个"采购管理"入口拆为 4 个独立 Dashboard 功能入口——采购汇总、待采购、已采购、已到货
- **独立页面**：每个入口对应独立页面（4 文件/页），不再共用 Tab 切换页面
- **保留子页面**：form 页（完善采购信息）、detail 页（详情）、inbound 页（入库）保持不变

### 新增权限码

| 菜单 | 权限码 |
|------|--------|
| 采购汇总 | `purchase:read` |
| 待采购 | `purchase:pending:read` |
| 已采购 | `purchase:purchased:read` |
| 已到货 | `purchase:arrived:read` |

## Capabilities

### New Capabilities

- `procurement-menu-split`: Web 端采购管理菜单拆分——将原有的单一"采购管理"菜单拆分为采购汇总、待采购、已采购、已到货四个独立菜单，每个菜单独立路由 + 独立权限码，各状态页面使用内联 Modal 完成操作
- `miniapp-procurement-menu-split`: 小程序端采购管理入口拆分——将原有的单一"采购管理"功能入口拆分为四个独立入口，每个入口对应独立页面，不再使用 Tab 切换

### Modified Capabilities

- `purchase-order-ui`: 采购管理页面不再使用 Tab 切换 UI，改为四个独立页面；移除手动新建采购单功能；待采购列表操作改为"采购完成"内联 Modal，已采购列表操作改为"入库"内联 Modal
- `miniapp-purchase-order`: 采购管理列表页不再包含 Tab 切换，拆分为四个独立列表页；Dashboard 功能注册表新增四个入口替代原有单一入口

## Impact

### 受影响的文件

**Web 端：**
- `src/router/index.js` — 新增 4 条路由，移除 `/purchase-orders/new`
- `src/views/dashboard/DashboardLayout.vue` — 4 个菜单项替换 1 个
- `src/views/purchase-order/PurchaseOrderListPage.vue` — 重构为汇总页（移除 Tab）
- 新增 `src/views/purchase-order/PendingPurchasePage.vue` — 待采购页
- 新增 `src/views/purchase-order/PurchasedPurchasePage.vue` — 已采购页
- 新增 `src/views/purchase-order/ArrivedPurchasePage.vue` — 已到货页

**小程序端：**
- `app.json` — 注册 3 个新页面
- `pages/dashboard/dashboard.js` — ALL_FEATURES 注册 4 个入口
- `pages/purchase-order/list.*` — 重构为汇总页
- 新增 `pages/purchase-order/pending.*`（4 文件）
- 新增 `pages/purchase-order/purchased.*`（4 文件）
- 新增 `pages/purchase-order/arrived.*`（4 文件）

**不受影响：**
- 后端 API 层
- 采购申请模块
- 供应商管理模块
- 仓库管理模块
