## Context

当前 `t_purchase_order` 通过 `purchase_request_id` UNIQUE 实现 1:1 关联。采购申请无编号字段。采购管理表单选择申请时仅显示名称。

## Goals / Non-Goals

**Goals:**
- 采购申请自动生成编号 `PRQ-{year}-{seq}`（参照 `PRJ-2026-001`）
- 采购记录支持关联多个申请（1:N），通过中间表 `t_purchase_order_item` 实现
- 一个申请只能被一条采购记录关联（UNIQUE 约束）
- 选择申请时展示完整信息并排除已关联的申请

**Non-Goals:**
- 不修改采购申请审批流程
- 不修改仓库入库逻辑

## Decisions

### 决策 1：采购申请编号格式

**选择**：`PRQ-{年份}-{4位序号}`，如 `PRQ-2026-0001`。

生成逻辑复用 `ProjectApplicationService.generateProjectCode()` 模式——从数据库查询当年最大编号，+1 生成新编号。

### 决策 2：中间表设计

**选择**：
```sql
CREATE TABLE t_purchase_order_item (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT NOT NULL REFERENCES t_purchase_order(id),
    purchase_request_id BIGINT NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_item_request UNIQUE (purchase_request_id)
);
```

`purchase_request_id` UNIQUE 确保一个申请不被重复关联。

### 决策 3：available 端点

**选择**：`GET /api/purchase-requests/available` — 返回 status=2（已通过）且 `id NOT IN (SELECT purchase_request_id FROM t_purchase_order_item)` 的申请。

响应复用 `PurchaseRequestResponse`，已包含 projectName, name, specification, quantity, unitLabel, arrivalDate 等全部所需字段。

### 决策 4：已有数据迁移

**选择**：迁移 SQL 将现有 `purchase_order.purchase_request_id` 数据写入中间表后删除列。若无关数据或无中间表数据则跳过。
