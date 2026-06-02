## Context

当前系统已有 project、purchaserequest、task、user 四个业务模块。所有模块遵循 DDD 四层架构。本次新增 warehouse 模块，管理仓库、物料、库存余量和出入库流水。

## Goals / Non-Goals

**Goals:**
- 仓库 CRUD（code/name/location/status，软删除）
- 物料 CRUD（code/name/spec/unit，软删除），物料主数据不含库存量
- `t_stock_balance` 记录每个仓库每个物料的当前库存量（UNIQUE warehouse_id + material_id）
- 出入库流水记录（warehouse_id/material_id/type/quantity/operator_id + 可选关联）
- 入库: INSERT stock_record + UPSERT stock_balance (quantity += N)
- 出库: 校验对应仓库物料库存 → INSERT stock_record + UPDATE stock_balance (quantity -= N)
- 前端独立菜单 + 三个管理页面
- 10 条细粒度权限

**Non-Goals:**
- 物料分类/类别管理
- 库存盘点、调拨、预警
- 批次/有效期管理

## Decisions

### 决策 1: 库存模型

**选择**: 三表模型 —— `t_material`（主数据）+ `t_stock_balance`（余量）+ `t_stock_record`（流水）

```
t_material           t_stock_balance              t_stock_record
┌────────────┐      ┌──────────────────────┐     ┌──────────────────────┐
│ id         │◄─────│ material_id FK       │     │ material_id FK       │
│ code (UK)  │      │ warehouse_id FK      │     │ warehouse_id FK      │
│ name       │      │ quantity             │     │ type (1=IN, 2=OUT)  │
│ spec       │      │ UNIQUE(warehouse_id, │     │ quantity             │
│ unit       │      │         material_id) │     │ purchase_request_id  │
│ is_deleted │      └──────────────────────┘     │ project_id           │
└────────────┘                                   │ operator_id FK       │
     t_warehouse                                 │ created_at           │
     ┌────────────┐                              └──────────────────────┘
     │ id         │◄───────────────────────────── warehouse_id FK
     │ code (UK)  │
     │ name       │
     │ location   │
     │ status     │
     │ is_deleted │
     └────────────┘
```

**理由**: 仓库维度的库存追踪是仓储管理的核心需求，单靠 `t_material.quantity` 无法回答"物料 X 在哪个仓库有多少库存"。

### 决策 2: 出入库事务模型

```java
@Transactional
public void recordStock(StockRecordCommand cmd) {
    if (cmd.type() == OUT) {
        StockBalance balance = stockBalanceRepo
            .findByWarehouseAndMaterial(cmd.warehouseId(), cmd.materialId())
            .orElseThrow(() -> BusinessException(STOCK_INSUFFICIENT));
        if (balance.quantity() < cmd.quantity())
            throw BusinessException(STOCK_INSUFFICIENT);
    }
    stockRecordRepo.save(cmd);
    stockBalanceRepo.upsert(cmd.warehouseId(), cmd.materialId(), delta);
}
```

### 决策 3: 权限

| code | 说明 | 适用 |
|------|------|------|
| warehouse:read | 查看仓库 | GET |
| warehouse:create | 创建仓库 | POST |
| warehouse:update | 编辑仓库 | PUT |
| warehouse:delete | 删除仓库 | DELETE |
| material:read | 查看物料 | GET |
| material:create | 创建物料 | POST |
| material:update | 编辑物料 | PUT |
| material:delete | 删除物料 | DELETE |
| stock:in | 入库 | POST (type=IN) |
| stock:out | 出库 | POST (type=OUT) |

### 决策 4: 前端页面

- `/warehouses` → WarehouseListPage（表格+弹窗）
- `/materials` → MaterialListPage（表格+弹窗，含各仓库库存）
- `/stock-records` → StockRecordListPage（筛选+入库/出库弹窗）

### 决策 5: stock_record 不可删除

出入库记录作为审计数据，不提供删除接口。仓库和物料支持软删除。

## Risks / Trade-offs

- **[风险] 并发出入库** → `@Transactional` + 行锁，后续可加乐观锁
- **[风险] UPSERT 竞态** → PostgreSQL `INSERT ... ON CONFLICT DO UPDATE` 原子操作
