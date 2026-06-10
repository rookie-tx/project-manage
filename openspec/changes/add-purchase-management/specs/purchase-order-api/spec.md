## ADDED Requirements

### Requirement: 创建采购记录

系统 SHALL 在 `POST /api/purchase-orders` 端点创建采购记录，关联已审批通过的采购申请。

#### Scenario: 成功创建

- **WHEN** 用户提交完整的采购数据（purchaseRequestId、supplierName、unitPrice、totalAmount、purchaseDate）及已上传的文件 ID
- **THEN** 系统创建采购记录，状态初始为"采购中"，回填关联文件的 business_id，返回完整数据

#### Scenario: 采购申请不存在

- **WHEN** 提交的 purchaseRequestId 不存在或已被软删除
- **THEN** 系统返回错误码 PO_PR_NOT_FOUND

#### Scenario: 重复创建

- **WHEN** 同一采购申请已存在采购记录（UNIQUE 约束）
- **THEN** 系统返回错误码 PO_DUPLICATE

#### Scenario: 必填校验

- **WHEN** 缺少必填字段（采购申请、供应商名称、单价、总金额、采购日期）
- **THEN** 系统返回参数校验错误

### Requirement: 采购记录列表

系统 SHALL 在 `GET /api/purchase-orders` 端点返回分页列表，支持按状态和项目筛选。

#### Scenario: 无条件查询

- **WHEN** 请求不带筛选参数
- **THEN** 系统返回第 1 页数据（每页 20 条），按创建时间倒序

#### Scenario: 按状态筛选

- **WHEN** 请求带 status 参数
- **THEN** 系统仅返回该状态的采购记录

#### Scenario: 按项目筛选

- **WHEN** 请求带 projectId 参数
- **THEN** 系统通过关联的采购申请查询所属项目，仅返回该项目下的采购记录

### Requirement: 采购记录详情

系统 SHALL 在 `GET /api/purchase-orders/{id}` 端点返回采购记录完整信息，含关联文件。

#### Scenario: 成功查询

- **WHEN** 请求存在的采购记录 ID
- **THEN** 系统返回完整字段及关联的文件列表（含原始文件名和访问 URL）

#### Scenario: 不存在

- **WHEN** 请求不存在的 ID 或已软删除
- **THEN** 系统返回错误码 PO_NOT_FOUND

### Requirement: 更新采购记录

系统 SHALL 在 `PUT /api/purchase-orders/{id}` 端点支持更新，仅"采购中"状态可编辑。

#### Scenario: 成功更新

- **WHEN** 状态为"采购中"时更新字段
- **THEN** 系统更新成功，返回更新后数据

#### Scenario: 非采购中不可编辑

- **WHEN** 状态不是"采购中"
- **THEN** 系统返回错误码 PO_STATUS_INVALID

### Requirement: 软删除

系统 SHALL 在 `DELETE /api/purchase-orders/{id}` 端点支持软删除。

#### Scenario: 成功删除

- **WHEN** 请求存在的采购记录 ID
- **THEN** 系统标记 is_deleted=1

### Requirement: 状态流转

系统 SHALL 在 `PUT /api/purchase-orders/{id}/status` 端点支持状态变更。

#### Scenario: 采购中→已采购

- **WHEN** 传入 status=1
- **THEN** 系统更新状态为"已采购"

#### Scenario: 已采购→已到货

- **WHEN** 传入 status=2
- **THEN** 系统更新状态为"已到货"

#### Scenario: 非法流转

- **WHEN** 传入的状态不合法（如跳过已采购直接到已到货）
- **THEN** 系统返回错误码 PO_STATUS_INVALID
