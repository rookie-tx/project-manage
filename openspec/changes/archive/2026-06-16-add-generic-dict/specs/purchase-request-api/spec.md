## MODIFIED Requirements

### Requirement: 采购申请响应包含物料信息

系统 SHALL 在采购申请列表和详情响应中返回关联的物料信息，单位相关字段使用字典 code（String 类型）。

#### Scenario: 列表响应

- **WHEN** 查询采购申请列表
- **THEN** 每条记录包含 `materialId`、`materialName`、`materialSpec`、`materialUnit` 字段（均为 String 或 Long 类型），`unitCode` 字段为 String 类型的字典 code（如 "TON"），不再提供 `unitLabel` 字段（前端根据 `unitCode` 自行查字典获取中文标签）

#### Scenario: 详情响应

- **WHEN** 查询采购申请详情
- **THEN** 响应包含 `materialId`、`materialName`、`materialSpec`、`materialUnit` 字段，`unitCode` 为 String 类型的字典 code

### Requirement: 创建采购申请

系统 SHALL 在 `POST /api/purchase-requests` 端点创建新的采购申请，物料信息通过 `materialId` 从物料库关联。

#### Scenario: 成功创建

- **WHEN** 用户提交完整的采购申请数据（projectId、materialId、brand、quantity、arrivalDate）
- **THEN** 系统根据 materialId 查询物料表获取 name、spec、unit（字典 code），自动填入采购申请，状态初始为"待提交"，返回 HTTP 200 及完整数据（含物料信息）

#### Scenario: 物料不存在

- **WHEN** 用户提交的 materialId 在 t_material 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4004

#### Scenario: 项目不存在

- **WHEN** 用户提交的 projectId 在 t_project 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4001

#### Scenario: 必填校验失败

- **WHEN** 用户未填写 materialId 字段
- **THEN** 系统返回参数校验错误，错误码 1002
