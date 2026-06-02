## ADDED Requirements

### Requirement: 查询仓库列表
系统 SHALL 在 `GET /api/warehouses` 端点返回所有未删除仓库的列表。

#### Scenario: 查询仓库列表
- **WHEN** 用户请求 `GET /api/warehouses`
- **THEN** 系统返回仓库列表，每条包含 id、code、name、location、status、createdAt

### Requirement: 创建仓库
系统 SHALL 在 `POST /api/warehouses` 端点创建新仓库。

#### Scenario: 成功创建仓库
- **WHEN** 用户请求 `POST /api/warehouses`，请求体 `{ "code": "WH-001", "name": "一号仓库", "location": "厂区东侧" }`
- **THEN** 系统创建仓库，status 默认 1（启用），返回仓库详情

#### Scenario: 编码重复
- **WHEN** 用户创建 code 已存在的仓库
- **THEN** 系统返回错误提示

### Requirement: 编辑仓库
系统 SHALL 在 `PUT /api/warehouses/{id}` 端点编辑仓库信息。

#### Scenario: 成功编辑
- **WHEN** 用户更新仓库名称和位置
- **THEN** 系统保存变更

### Requirement: 软删除仓库
系统 SHALL 在 `DELETE /api/warehouses/{id}` 端点软删除仓库。

#### Scenario: 成功删除
- **WHEN** 用户删除无关联数据的仓库
- **THEN** 系统标记 is_deleted=1
