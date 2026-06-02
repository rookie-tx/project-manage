## ADDED Requirements

### Requirement: 查询物料列表
系统 SHALL 在 `GET /api/materials` 端点返回所有未删除物料的列表。

#### Scenario: 查询物料列表
- **WHEN** 用户请求 `GET /api/materials`
- **THEN** 系统返回物料列表，每条包含 id、code、name、spec、unit、createdAt

### Requirement: 创建物料
系统 SHALL 在 `POST /api/materials` 端点创建新物料（主数据）。

#### Scenario: 成功创建物料
- **WHEN** 用户请求 `POST /api/materials`，请求体 `{ "code": "MAT-001", "name": "钢筋", "spec": "Φ12mm", "unit": "吨" }`
- **THEN** 系统创建物料

#### Scenario: 编码重复
- **WHEN** 用户创建 code 已存在的物料
- **THEN** 系统返回错误提示

### Requirement: 编辑物料
系统 SHALL 在 `PUT /api/materials/{id}` 端点编辑物料信息（名称、规格、单位）。

#### Scenario: 成功编辑
- **WHEN** 用户更新物料名称、规格、单位
- **THEN** 系统保存变更

### Requirement: 查询物料在各仓库的库存
系统 SHALL 在 `GET /api/materials/{id}/stock` 端点通过 `t_stock_balance` 表查询该物料在各仓库的库存量。

#### Scenario: 查询分布
- **WHEN** 用户请求 `GET /api/materials/1/stock`
- **THEN** 系统返回各仓库名称及对应库存量列表
