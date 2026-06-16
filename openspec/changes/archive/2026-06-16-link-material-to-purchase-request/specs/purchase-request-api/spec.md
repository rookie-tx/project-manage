## MODIFIED Requirements

### Requirement: 创建采购申请

系统 SHALL 在 `POST /api/purchase-requests` 端点创建新的采购申请，物料信息通过 `materialId` 从物料库关联。

#### Scenario: 成功创建

- **WHEN** 用户提交完整的采购申请数据（projectId、materialId、brand、quantity、arrivalDate）
- **THEN** 系统根据 materialId 查询物料表获取 name、spec、unit，自动填入采购申请，状态初始为"待提交"，返回 HTTP 200 及完整数据（含物料信息）

#### Scenario: 物料不存在

- **WHEN** 用户提交的 materialId 在 t_material 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4004

#### Scenario: 项目不存在

- **WHEN** 用户提交的 projectId 在 t_project 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4001

#### Scenario: 必填校验失败

- **WHEN** 用户未填写 materialId 字段
- **THEN** 系统返回参数校验错误，错误码 1002

### Requirement: 更新采购申请

系统 SHALL 在 `PUT /api/purchase-requests/{id}` 端点支持部分更新。

#### Scenario: 更新基本信息

- **WHEN** 采购申请状态为"待提交"，用户修改品牌、数量等字段并提交
- **THEN** 系统更新对应字段，若 materialId 变更则重新从物料库获取 name/spec/unit，返回 HTTP 200 及更新后数据

#### Scenario: 非待提交状态不可编辑

- **WHEN** 采购申请状态为"审批中"或其他非待提交状态，用户尝试编辑
- **THEN** 系统返回 BusinessException，错误码 4003

## ADDED Requirements

### Requirement: 采购申请响应包含物料信息

系统 SHALL 在采购申请列表和详情响应中返回关联的物料信息。

#### Scenario: 列表响应

- **WHEN** 查询采购申请列表
- **THEN** 每条记录包含 materialId、materialName、materialSpec、materialUnit 字段

#### Scenario: 详情响应

- **WHEN** 查询采购申请详情
- **THEN** 响应包含 materialId、materialName、materialSpec、materialUnit 字段

### Requirement: 物料不存在错误码

采购申请模块 SHALL 新增物料不存在错误码。

| 错误码 | 含义 |
|--------|------|
| 4004 | 物料不存在 |
