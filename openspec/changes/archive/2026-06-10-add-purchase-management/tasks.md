## 1. 数据库

- [x] 1.1 在 `schema.sql` 中创建 `t_purchase_order` 表（id, purchase_request_id UNIQUE, supplier_name, supplier_phone, unit_price, total_amount, purchase_date, expected_arrival_date, status, remark, is_deleted, created_by, created_at, updated_at）

## 2. 后端模块

- [x] 2.1 创建 `domain/PurchaseOrder.java` — 实体 + 静态工厂方法
- [x] 2.2 创建 `domain/PurchaseOrderStatus.java` — 状态枚举（采购中/已采购/已到货）
- [x] 2.3 创建 `domain/PurchaseOrderRepository.java` — 仓储接口
- [x] 2.4 创建 `infrastructure/PurchaseOrderPO.java` + `PurchaseOrderPoMapper.java` — PO + MapStruct 映射
- [x] 2.5 创建 `infrastructure/PurchaseOrderMapper.java` + `PurchaseOrderMapper.xml` — MyBatis
- [x] 2.6 创建 `infrastructure/PurchaseOrderRepositoryImpl.java` — 仓储实现
- [x] 2.7 创建 `api/CreatePurchaseOrderRequest.java` + `UpdatePurchaseOrderRequest.java` + `PurchaseOrderResponse.java` — DTO
- [x] 2.8 创建 `api/PurchaseOrderController.java` — REST 接口（CRUD + 状态流转）
- [x] 2.9 创建 `application/PurchaseOrderApplicationService.java` — 应用服务
- [x] 2.10 创建 `infrastructure/PurchaseOrderDomainConfig.java` — Bean 配置

## 3. Web SPA

- [x] 3.1 创建 `stores/purchaseOrder.js` — Pinia store
- [x] 3.2 创建 `views/purchase-order/PurchaseOrderListPage.vue`
- [x] 3.3 创建 `views/purchase-order/PurchaseOrderFormPage.vue`
- [x] 3.4 创建 `views/purchase-order/PurchaseOrderDetailPage.vue`
- [x] 3.5 修改 `router/index.js` + `DashboardLayout.vue` — 路由 + 导航菜单

## 4. 小程序

- [x] 4.1 创建 `api/purchaseOrder.js` — API 封装
- [x] 4.2 创建 `pages/purchase-order/list.*` (4 文件)
- [x] 4.3 创建 `pages/purchase-order/form.*` (4 文件)
- [x] 4.4 创建 `pages/purchase-order/detail.*` (4 文件)
- [x] 4.5 修改 `dashboard.js` + `app.json` — 入口 + 路由
