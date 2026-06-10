## ADDED Requirements

### Requirement: 采购申请自动编号

系统 SHALL 在创建采购申请时自动生成唯一编号，格式为 `PRQ-{年份}-{序号}`。

#### Scenario: 自动生成编号

- **WHEN** 用户创建采购申请
- **THEN** 系统从数据库查询当年最大序号，+1 生成编号如 `PRQ-2026-0001`

#### Scenario: 编号唯一

- **WHEN** 同一年已有 N 条申请
- **THEN** 新编号为 `PRQ-2026-{N+1 左补零为4位}`

### Requirement: 采购记录多申请关联

系统 SHALL 通过中间表 `t_purchase_order_item` 支持一条采购记录关联多个采购申请。

#### Scenario: 创建时关联多个申请

- **WHEN** 用户创建采购记录并选择多个采购申请
- **THEN** 系统在中间表中插入多条记录，每条关联一个申请

#### Scenario: 一个申请只能被关联一次

- **WHEN** 用户尝试将已被关联的申请再次关联到其他采购记录
- **THEN** 系统返回唯一约束冲突错误

### Requirement: 可选采购申请查询

系统 SHALL 在 `GET /api/purchase-requests/available` 端点返回已通过且未被关联的采购申请。

#### Scenario: 查询可用申请

- **WHEN** 请求 available 端点
- **THEN** 系统返回 status=2（已通过）且 purchase_request_id 不在 t_purchase_order_item 中的申请，含项目名称、物料名称、规格、数量、单位、到场时间
