## Context

当前采购管理 Web 端 `/purchase-orders` 路由对应单一页面 `PurchaseOrderListPage.vue`，通过 3 个 Tab（待采购 / 已采购 / 已到货）切换列表数据。所有 Tab 共用菜单项和权限码 `purchase:read`。小程序端同样在 `pages/purchase-order/list` 页面内用 3 个 Tab 切换。

需求是为后续权限精细化管理打基础——不同角色只看到自己有权限的状态页（采购员只看到"待采购"，库管员只看到"已采购"做入库，管理人员可看"汇总"全览）。

另外，采购单的生命周期已调整为：采购申请审批通过后由后端自动创建采购单，前端不再需要手动新建入口。

## Goals / Non-Goals

**Goals:**
- Web 端将 1 个"采购管理"菜单拆为 4 个独立菜单（采购汇总、待采购、已采购、已到货）
- 每个菜单对应独立路由和独立权限码
- 待采购页面提供"采购完成"内联 Modal（填写供应商信息 + 流转到已采购）
- 已采购页面提供"入库"内联 Modal（创建库存记录 + 流转到已到货）
- 已到货页面为只读列表
- 采购汇总页展示全部状态的采购单
- 移除手动新建采购单的路由入口
- 小程序端同步拆分 Dashboard 功能入口和页面

**Non-Goals:**
- 后端 API 层不做变更
- 采购申请模块不做变更
- 供应商管理、仓库管理不做变更
- 采购单详情页、独立编辑页保持不变
- 不改变采购单状态流转逻辑（待采购→已采购→已到货）

## Decisions

### 1. 路由结构

采用嵌套路径方案，以 `/purchase-orders` 为汇总根路径：

| 路由路径 | 组件 | 权限 |
|----------|------|------|
| `/purchase-orders` | `PurchaseOrderSummaryPage` | `purchase:read` |
| `/purchase-orders/pending` | `PendingPurchasePage` | `purchase:pending:read` |
| `/purchase-orders/purchased` | `PurchasedPurchasePage` | `purchase:purchased:read` |
| `/purchase-orders/arrived` | `ArrivedPurchasePage` | `purchase:arrived:read` |
| `/purchase-orders/:id` | `PurchaseOrderDetailPage` | 不变 |
| `/purchase-orders/:id/edit` | `PurchaseOrderFormPage` | `purchase:update` |

移除 `/purchase-orders/new`。

**理由**：保持路径在同一前缀下语义内聚，符合现有项目路由模式（如 `/procurement` 系列）。汇总页复用 `purchase:read` 权限码保持向后兼容。

### 2. 页面组件策略

采用**共享 composable + 4 个薄页面组件**方案：

```
src/
├── composables/
│   └── usePurchaseOrderList.js   # 共享逻辑
├── views/purchase-order/
│   ├── PurchaseOrderSummaryPage.vue    # 汇总（无状态过滤）
│   ├── PendingPurchasePage.vue         # 待采购（status=0）
│   ├── PurchasedPurchasePage.vue       # 已采购（status=1）
│   ├── ArrivedPurchasePage.vue         # 已到货（status=2）
│   ├── PurchaseOrderDetailPage.vue     # 不变
│   └── PurchaseOrderFormPage.vue       # 不变（仅编辑模式）
```

**理由**：四个页面的列表、分页、表格列完全相同，差异仅在于：(1) 状态过滤参数 (2) 操作按钮的可见性和行为 (3) 关联的 Modal。提取 composable 避免重复代码。

备选方案（4 个独立完整组件）被否决——代码重复太多，维护成本高。

### 3. 内联 Modal vs 独立编辑页

"采购完成"和"入库"操作使用**内联 Modal**，不导航到独立页面。

**理由**：
- 操作完成立即可见列表刷新，体验流畅
- 当前代码已验证此模式可行（`PurchaseOrderListPage.vue` 中已有 edit modal 和 inbound modal）
- 减少页面跳转，降低用户等待
- 独立编辑页 `/:id/edit` 仍然保留，供需要单独编辑的场景使用

### 4. 权限码设计

| 菜单 | 权限码 | 说明 |
|------|--------|------|
| 采购汇总 | `purchase:read` | 复用现有权限，向后兼容 |
| 待采购 | `purchase:pending:read` | 新增 |
| 已采购 | `purchase:purchased:read` | 新增 |
| 已到货 | `purchase:arrived:read` | 新增 |

操作权限保持不变：`purchase:update`（编辑/采购完成）、`stock:in`（入库）。

### 5. 小程序页面结构

小程序 4 个独立页面，共享列表逻辑通过公共 JS 模块 `pages/purchase-order/list-behavior.js` 提取：

```
pages/purchase-order/
├── list-behavior.js         # 共享：加载、分页、刷新逻辑
├── list.*                   # 汇总页（无 status 过滤）
├── pending.*                # 待采购页（status=0）
├── purchased.*              # 已采购页（status=1）
├── arrived.*                # 已到货页（status=2）
├── form.*                   # 不变
├── detail.*                 # 不变
└── inbound.*                # 不变
```

Dashboard `ALL_FEATURES` 注册 4 个入口替换原有 1 个 `purchaseOrder` 入口。

## Risks / Trade-offs

- **[风险] 现有角色已有 `purchase:read` 权限，拆分后看不到新增的 3 个菜单** → 部署前需给已有角色批量授予 `purchase:pending:read`、`purchase:purchased:read`、`purchase:arrived:read` 权限
- **[风险] 4 个页面初次加载时分别请求 API，无法像 Tab 切换时复用缓存** → store 本身不做缓存隔离，同一 store 实例跨页面共享，实际上数据会自然缓存；且不同权限用户通常只访问自己有权限的页面
- **[风险] 小程序页面文件数从 16 增至 28** → 提取 `list-behavior.js` 减少重复逻辑，每个页面仍是 4 文件的标准结构
- **[权衡] 汇总页 vs 直接列表** → 汇总页展示全部状态，和现有"待采购"Tab 的默认行为不同，需要明确标注各记录的状态

## Open Questions

- 待确认：后端自动创建采购单时，如果多个采购申请合并为一个采购单，关联关系如何确定？（当前实现是一个 PO 可关联多个 PR）
- 待确认：是否需要保留 `purchase:create` 权限码？（虽然前端移除新建入口，但后端 API 仍可能被直接调用）
