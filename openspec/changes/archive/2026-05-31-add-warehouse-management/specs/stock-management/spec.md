## ADDED Requirements

### Requirement: 入库操作
系统 SHALL 在 `POST /api/stock-records`（type=IN）时写入流水并增加对应仓库物料的库存余量。

#### Scenario: 新物料首次入库
- **WHEN** 某仓库某物料尚无库存记录，用户入库 100 单位
- **THEN** 系统插入流水记录，`t_stock_balance` 新增一行（warehouse_id, material_id, quantity=100）

#### Scenario: 已有物料再次入库
- **WHEN** 该物料在仓库已有库存 100，再次入库 50
- **THEN** `t_stock_balance.quantity` 更新为 150

### Requirement: 出库操作
系统 SHALL 在 `POST /api/stock-records`（type=OUT）时校验对应仓库物料库存充足后写入流水并扣减余量。

#### Scenario: 成功出库
- **WHEN** 仓库某物料库存为 100，用户出库 50
- **THEN** 系统插入流水，`t_stock_balance.quantity` 更新为 50

#### Scenario: 库存不足拒绝
- **WHEN** 仓库该物料库存为 30，用户请求出库 100
- **THEN** 系统返回错误 "库存不足"，不写入任何数据

#### Scenario: 该仓库无此物料库存
- **WHEN** 用户在仓库 A 出库从未在仓库 A 入库的物料
- **THEN** 系统返回错误 "库存不足"

### Requirement: 出入库关联业务单据
系统 SHALL 支持入库关联采购申请（purchase_request_id），出库关联项目（project_id）。

#### Scenario: 入库关联采购单
- **WHEN** 用户入库时传入 purchaseRequestId
- **THEN** 流水记录关联该采购单

### Requirement: 查询出入库记录
系统 SHALL 在 `GET /api/stock-records` 支持按仓库、物料、类型、时间范围筛选。

### Requirement: 查询仓库库存
系统 SHALL 在 `GET /api/warehouses/{id}/stock` 返回该仓库各物料的当前库存量。

#### Scenario: 查询仓库库存
- **WHEN** 用户请求 `GET /api/warehouses/1/stock`
- **THEN** 系统返回该仓库每种物料的名称及库存量

### Requirement: 操作人记录
系统 SHALL 在出入库时自动记录当前登录用户为操作人。

#### Scenario: 自动记录
- **WHEN** 已登录用户执行入库或出库
- **THEN** stock_record.operator_id 自动填充
