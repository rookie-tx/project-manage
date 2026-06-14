## Context

当前采购流程存在三个痛点：审批冗余（三级审批）、采购记录手动创建（重复录入）、入库割裂（不联动 PO）。本次优化以简化 BPMN、自动创建 PO、1:1 映射、入库联动为核心目标。

**约束**：遵循现有 DDD 四层架构；Flowable BPMN 引擎；PostgreSQL/MySQL 双 schema 同步维护。

## Goals / Non-Goals

**Goals:**
- BPMN 简化为单节点（项目经理），移除财务和总经理审批
- PR 审批通过后自动创建 PO（字段部分留空）
- PR : PO 改为 1:1（`t_purchase_order.purchase_request_id` UNIQUE FK）
- 采购管理页按状态 Tab 展示，待采购 Tab 可编辑供应商信息
- 入库管理页可查看已采购 PO，入库操作后 PO 自动流转为已到货

**Non-Goals:**
- 不修改 PR 表结构和 API 请求体
- 不影响已有 StockRecord 逻辑
- 不修改小程序采购申请页

## Decisions

### 1. BPMN 单节点

新 BPMN 仅保留一个 `userTask` — 项目经理审批：

```
start → pmApproval (assignee=${pmUserId})
         ├─ APPROVED → approvedEnd
         └─ REJECTED → rejectedEnd
```

`PurchaseRequestTaskCallback.validateBeforeStart` 仅校验 `pmUserId`。启动流程时只需传入 `pmUserId` 变量。

### 2. PR 审批通过自动创建 PO

`PurchaseRequestTaskCallback.onProcessStarted` 改为调用 `onTaskCompleted`（流程只有一个节点，任务完成即流程结束）。`onTaskCompleted` 中若 result 为 Approved，调用 `PurchaseOrderRepository` 创建 PO：

```
PO 自动创建字段映射：
  purchaseRequestId ← PR.id
  supplierName       ← null (留空)
  supplierPhone      ← null (留空)
  unitPrice          ← null
  totalAmount        ← 0 (留空)
  purchaseDate       ← null
  expectedArrivalDate ← PR.arrivalDate
  status             ← PURCHASING
  createdBy          ← PR.createdBy
```

### 3. PR : PO 一对一

```
t_purchase_order 变更：
  + purchase_request_id BIGINT UNIQUE REFERENCES t_purchase_request(id)

t_purchase_order_item: 删除表，移除所有相关代码
```

`CreatePurchaseOrderRequest` 简化为 `{ purchaseRequestId: Long }`。编辑时补充供应商信息走 PUT `/api/purchase-orders/{id}`。

### 4. 采购管理页 Tab 重构

```
┌──────────────────────────────────────────────┐
│ 采购管理                                      │
│ [待采购] [采购中] [已采购] [已到货]             │
├──────────────────────────────────────────────┤
│                                              │
│ 待采购 Tab:                                  │
│   展示 status=PURCHASING 且 supplierName 为空的 │
│   PO → 点击编辑 → 弹窗补填供应商+价格            │
│                                              │
│ 采购中 Tab:                                  │
│   展示 status=PURCHASING 且 supplierName 不为空 │
│   → 点击"采购完成" → status → PURCHASED        │
│                                              │
│ 已采购 Tab:                                  │
│   展示 status=PURCHASED                       │
│   → 点击"入库" → 弹窗选仓库/物料/数量           │
│      → 创建 StockRecord(type=IN)             │
│      → PO status → ARRIVED                   │
│                                              │
│ 已到货 Tab:                                  │
│   展示 status=ARRIVED (只读)                  │
└──────────────────────────────────────────────┘
```

API 新增 `GET /api/purchase-orders?status=&tab=pending` 区分是否已填供应商。

### 5. 入库联动 API

在 `MaterialController` 新增接口：

```
GET /api/stock-records/pending-inbound
  → 查询所有 status=PURCHASED 的 PO，返回列表（含 PR 信息和仓库物料选择）
```

入库操作复用现有 `POST /api/stock-records`，成功后额外：
```
PUT /api/purchase-orders/{id}/status { status: 2 }  →  PO → ARRIVED
```

前端 `StockRecordListPage` 新增"待入库"区域，选择 PO 后弹出入库表单（仓库/物料/数量）。

### 6. 状态流转

```
PR: PENDING → APPROVING ──┬→ APPROVED ──→ (自动创建 PO)
                          └→ REJECTED

PO: PURCHASING ──编辑──→ PURCHASING(已填供应商) ──采购完成──→ PURCHASED ──入库──→ ARRIVED
     (自动创建)                                                (入库联动创建StockRecord)
```

## Risks / Trade-offs

- **数据迁移**：现有 PO 没有 `purchase_request_id`，需处理历史数据的 null 值 → 新字段 NOT NULL 但有默认逻辑，或允许 NULL 仅对新数据强制
- **BPMN 变更**：正在运行中的旧流程实例可能受影响 → 新 BPMN deployment 时使用新 key 或先终止旧实例
- **PurchaseRequestTaskCallback 重构**：原有多审批人校验逻辑需完全替换
- **前端破坏性变更**：采购管理页从单列表变为 Tab 模式，create 接口参数变更影响现有页面
