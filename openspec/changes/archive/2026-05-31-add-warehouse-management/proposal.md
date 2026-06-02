## Why

当前系统覆盖项目管理、采购申请和审批流程，但缺少物资库存管理能力。采购申请通过后物资入库、项目施工领用物资出库等场景无法在系统中追踪。需要新增仓库管理模块，提供仓库、物料、出入库流水的基础管理能力，并与现有项目和采购模块关联。

## What Changes

- 新建 **warehouse 模块**（后端 DDD 四层），包含仓库 CRUD、物料 CRUD、出入库流水管理
- 数据库新增 `t_warehouse`、`t_material`、`t_stock_record` 三张表
- `t_material` 表包含 `quantity` 字段记录当前库存总量，出入库时事务更新
- `t_stock_record` 记录出入库流水，入库关联 `purchase_request_id`，出库关联 `project_id`，操作人关联 `t_user`
- 出库时校验库存是否充足，不足则拒绝
- 种子数据新增 8 条仓库/物料/库存权限
- 前端新增仓库管理、物料管理、出入库记录三个页面
- 前端独立菜单项 **"仓库管理"**，需 `warehouse:read` 权限可见

## Capabilities

### New Capabilities
- `warehouse-crud`: 仓库基础信息 CRUD（列表、创建、编辑、软删除）
- `material-crud`: 物料管理 CRUD（列表、创建、编辑、软删除），包含当前库存量
- `stock-management`: 出入库管理（入库/出库流水记录，库存校验，事务更新物料库存量）
- `warehouse-ui`: 前端仓库管理页面、物料管理页面、出入库记录页面

### Modified Capabilities
<!-- None -->

## Impact

| 影响范围 | 说明 |
|----------|------|
| `ai-agent-server/` warehouse 模块（新建） | 完整 DDD 四层：api/application/domain/infrastructure |
| 数据库 | 新增 `t_warehouse`、`t_material`、`t_stock_record` 三张表 + 种子权限 |
| `ai-agent-client/` router | 新增 `/warehouses`、`/materials`、`/stock-records` 路由 |
| `ai-agent-client/` menu | DashboardLayout 新增"仓库管理"菜单项（需 `warehouse:read` 权限） |
| `ErrorCode` | 新增 7xxx 范围库存不足等错误码 |
| 权限体系 | 新增 8 条权限：warehouse:read/create/update/delete, material:read/create/update/delete, stock:in, stock:out |
