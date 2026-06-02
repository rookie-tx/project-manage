## 1. 数据库

- [x] 1.1 创建 `t_warehouse` 表（id, code UK, name, location, status SMALLINT, is_deleted, created_at, updated_at）
- [x] 1.2 创建 `t_material` 表（id, code UK, name, spec, unit, is_deleted, created_at, updated_at）—— 不含库存字段
- [x] 1.3 创建 `t_stock_balance` 表（id, warehouse_id FK, material_id FK, quantity DOUBLE DEFAULT 0, UNIQUE(warehouse_id, material_id)）
- [x] 1.4 创建 `t_stock_record` 表（id, warehouse_id FK, material_id FK, type SMALLINT 1=IN 2=OUT, quantity, purchase_request_id, project_id, operator_id FK, created_at）
- [x] 1.5 种子数据新增 10 条权限（warehouse:*, material:*, stock:in, stock:out）+ admin 自动获得
- [x] 1.6 添加序列修正语句

## 2. Warehouse 领域层

- [x] 2.1 创建 `WarehouseStatus` 枚举（ACTIVE/DISABLED）
- [x] 2.2 创建 `Warehouse` 领域实体 + `fromPersistence` 工厂方法
- [x] 2.3 创建 `Material` 领域实体 + `fromPersistence` 工厂方法
- [x] 2.4 创建 `StockRecord` 领域实体 + 密封接口/枚举区分入库/出库类型
- [x] 2.5 创建 `WarehouseRepository` 接口（CRUD + 软删除）
- [x] 2.6 创建 `MaterialRepository` 接口（CRUD + 软删除）
- [x] 2.7 创建 `StockBalanceRepository` 接口（findByWarehouseAndMaterial, upsert）
- [x] 2.8 创建 `StockRecordRepository` 接口（save + 分页筛选查询）

## 3. Warehouse 基础设施层

- [x] 3.1 创建 `WarehousePO`、`MaterialPO`、`StockBalancePO`、`StockRecordPO`
- [x] 3.2 创建 `WarehouseMapper` + XML
- [x] 3.3 创建 `MaterialMapper` + XML（不含 quantity 字段）
- [x] 3.4 创建 `StockBalanceMapper` + XML（upsert 使用 ON CONFLICT DO UPDATE）
- [x] 3.5 创建 `StockRecordMapper` + XML（insert, selectAll 含筛选）
- [x] 3.6 创建 MapStruct Mapper（WarehousePoMapper, MaterialPoMapper, StockBalancePoMapper, StockRecordPoMapper）
- [x] 3.7 创建四个 RepositoryImpl + WarehouseDomainConfig

## 4. Warehouse 应用层

- [x] 4.1 创建 `StockApplicationService`（@Transactional: INSERT stock_record + UPSERT/UPDATE stock_balance, 出库校验库存）
- [x] 4.2 创建 `WarehouseDtoMapper` MapStruct 接口

## 5. Warehouse API 层

- [x] 5.1 创建 `WarehouseController`（GET/POST/PUT/DELETE /api/warehouses）+ Request DTO
- [x] 5.2 创建 `MaterialController`（GET/POST/PUT/DELETE /api/materials, GET /api/materials/{id}/stock 查 stock_balance）+ Request DTO
- [x] 5.3 创建 `StockRecordController`（GET /api/stock-records, POST 入库/出库; GET /api/warehouses/{id}/stock 查库存）
- [x] 5.4 ErrorCode 新增 `STOCK_INSUFFICIENT(7010)` + i18n
- [x] 5.5 Controller 写操作添加 `@RequirePermission` 注解

## 6. 前端 — 仓库管理

- [x] 6.1 创建 `useWarehouseStore` Pinia store
- [x] 6.2 创建 `src/views/warehouse/WarehouseListPage.vue`
- [x] 6.3 路由添加 `/warehouses`

## 7. 前端 — 物料管理

- [x] 7.1 创建 `useMaterialStore` Pinia store
- [x] 7.2 创建 `src/views/warehouse/MaterialListPage.vue`
- [x] 7.3 路由添加 `/materials`

## 8. 前端 — 出入库管理

- [x] 8.1 创建 `useStockRecordStore` Pinia store
- [x] 8.2 创建 `src/views/warehouse/StockRecordListPage.vue`（筛选 + 入库/出库弹窗）
- [x] 8.3 路由添加 `/stock-records`

## 9. 前端 — 菜单

- [x] 9.1 DashboardLayout 新增"仓库管理"菜单（warehouse:read 权限，含子菜单：仓库/物料/出入库）

## 10. 集成与验证

- [x] 10.1 编写 `StockApplicationService` 单元测试（入库成功、出库成功、库存不足）
- [x] 10.2 启动后端验证 CRUD 和出入库端点
- [x] 10.3 启动前端验证页面和菜单
