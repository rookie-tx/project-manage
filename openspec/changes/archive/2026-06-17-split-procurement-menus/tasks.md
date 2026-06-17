## 1. Web 端 — 共享逻辑提取

- [x] 1.1 创建 `src/composables/usePurchaseOrderList.js`，从 `PurchaseOrderListPage.vue` 提取共享逻辑：列表加载、分页、表格列定义、日期/金额格式化、编辑 Modal 状态与方法、入库 Modal 状态与方法
- [x] 1.2 composable 接收 `statusFilter` 参数（`null`=汇总, `0`=待采购, `1`=已采购, `2`=已到货），自动按状态过滤 API 请求
- [x] 1.3 composable 返回 `showEditAction`/`showInboundAction` 等 computed 属性，由调用方决定是否渲染操作按钮

## 2. Web 端 — 页面组件与路由

- [x] 2.1 创建 `PurchaseOrderSummaryPage.vue`（采购汇总），使用 composable，`statusFilter=null`，展示全部状态采购单，状态列显示标签
- [x] 2.2 创建 `PendingPurchasePage.vue`（待采购），使用 composable，`statusFilter=0`，包含"采购完成"按钮和内联编辑 Modal
- [x] 2.3 创建 `PurchasedPurchasePage.vue`（已采购），使用 composable，`statusFilter=1`，包含"入库"按钮和内联入库 Modal
- [x] 2.4 创建 `ArrivedPurchasePage.vue`（已到货），使用 composable，`statusFilter=2`，纯只读列表
- [x] 2.5 更新 `src/router/index.js`：新增汇总、pending、purchased、arrived 四条路由，移除 `/purchase-orders/new` 路由，各路由绑定对应权限码 meta

## 3. Web 端 — 菜单改造

- [x] 3.1 更新 `DashboardLayout.vue` 导航菜单：移除原有"采购管理"菜单项，新增"采购汇总"、"待采购"、"已采购"、"已到货"四个菜单项
- [x] 3.2 菜单项绑定对应权限码：`purchase:read`、`purchase:pending:read`、`purchase:purchased:read`、`purchase:arrived:read`
- [x] 3.3 确认 `handleMenuClick` 中 menu key 到路由的映射正确

## 4. Web 端 — 清理与验证

- [x] 4.1 删除或移除 `PurchaseOrderListPage.vue` 的引用（路由已移除对该文件的引用，原文件保留在磁盘作为备份）
- [x] 4.2 验证 `PurchaseOrderFormPage.vue` 仅剩编辑模式（已移除 `isEdit` 判断、创建逻辑、文件上传强制要求、`/purchase-requests/available` 调用）
- [x] 4.3 检查 `PurchaseOrderDetailPage.vue` 仍然正常工作（路由 `:id` 路径和 meta 权限未改变）

## 5. 小程序端 — 共享逻辑提取

- [x] 5.1 创建 `pages/purchase-order/list-behavior.js`，从 `list.js` 提取共享逻辑：加载、分页、下拉刷新、数据格式化、跳转 form/inbound/detail
- [x] 5.2 共享模块导出工厂函数，接收 `statusFilter` 参数，返回 Page 可用的 data/methods 对象

## 6. 小程序端 — 新页面创建

- [x] 6.1 改造 `pages/purchase-order/list.*` 为采购汇总页（移除 Tab 切换 UI，展示全部状态列表）
- [x] 6.2 创建 `pages/purchase-order/pending.*`（4 文件），待采购独立页，status=0
- [x] 6.3 创建 `pages/purchase-order/purchased.*`（4 文件），已采购独立页，status=1
- [x] 6.4 创建 `pages/purchase-order/arrived.*`（4 文件），已到货独立页，status=2，只读
- [x] 6.5 更新 `app.json` 的 `pages` 数组，注册 pending、purchased、arrived 三个新页面

## 7. 小程序端 — Dashboard 入口

- [x] 7.1 更新 `pages/dashboard/dashboard.js` 中 `ALL_FEATURES`，移除原有 `purchaseOrder` 入口，新增 `purchaseSummary`、`pendingPurchase`、`purchasedPurchase`、`arrivedPurchase` 四个入口
- [x] 7.2 每个入口绑定对应权限码和跳转 URL

## 8. 验证

- [x] 8.1 Web 端：已验证路由和菜单配置——四个菜单项正确映射到对应路由，权限码配置正确
- [x] 8.2 Web 端：待采购页面 `PendingPurchasePage.vue` 包含内联编辑 Modal，`showEditAction` computed 在 statusFilter=0 时为 true
- [x] 8.3 Web 端：已采购页面 `PurchasedPurchasePage.vue` 包含内联入库 Modal，`showInboundAction` computed 在 statusFilter=1 时为 true
- [x] 8.4 Web 端：`/purchase-orders/new` 路由已从 `router/index.js` 中移除
- [x] 8.5 Web 端：`/:id` 详情页和 `/:id/edit` 编辑页路由保留，Vite 构建成功
- [x] 8.6 小程序端：`app.json` 已注册 3 个新页面，`dashboard.js` ALL_FEATURES 配置 4 个入口
- [x] 8.7 小程序端：待采购页 `pending.js` 中 `handleEdit` 导航到 `/pages/purchase-order/form?id=`
- [x] 8.8 小程序端：已采购页 `purchased.js` 中 `handleInbound` 导航到 `/pages/purchase-order/inbound`
