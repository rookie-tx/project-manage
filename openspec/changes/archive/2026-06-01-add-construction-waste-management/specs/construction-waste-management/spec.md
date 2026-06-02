## ADDED Requirements

### Requirement: 创建建筑废渣运出记录

系统 SHALL 允许有 `waste:create` 权限的用户创建废渣运出记录。创建时必须关联项目、上传至少一张照片、填写废渣种类、数量、单位、运输信息和运出日期。

#### Scenario: 成功创建运出记录

- **WHEN** 有权限的用户提交创建请求，包含项目 ID、废渣种类、数量、单位、车牌号、司机姓名、处置去向、运出日期和至少一个已上传的文件 ID
- **THEN** 系统在 `t_construction_waste` 表中插入记录，将关联的 `t_upload_file` 记录的 `business_id` 更新为该记录的 ID，`business_type` 更新为 `construction_waste`，返回创建的记录详情

#### Scenario: 未上传照片

- **WHEN** 用户提交创建请求但 `fileIds` 为空数组
- **THEN** 系统返回错误码 `WASTE_PHOTO_REQUIRED`，不创建记录

#### Scenario: 未关联项目

- **WHEN** 用户提交创建请求但 `projectId` 为空
- **THEN** 系统返回校验错误，提示项目为必填项

#### Scenario: 无权限

- **WHEN** 没有 `waste:create` 权限的用户尝试创建记录
- **THEN** 系统返回错误码 `ACCESS_DENIED`

### Requirement: 查询建筑废渣运出记录列表

系统 SHALL 允许有 `waste:read` 权限的用户查询废渣运出记录列表，支持按项目、日期范围和废渣种类筛选。

#### Scenario: 无条件查询列表

- **WHEN** 有权限的用户请求 `GET /api/construction-wastes` 不带任何筛选参数
- **THEN** 系统返回所有未删除的记录列表，按创建时间倒序排列，每条记录包含 ID、项目名称、废渣种类、数量、运出日期、操作人信息

#### Scenario: 按项目筛选

- **WHEN** 用户请求列表并指定 `projectId` 参数
- **THEN** 系统仅返回属于该项目的废渣记录

#### Scenario: 按日期范围筛选

- **WHEN** 用户请求列表并指定 `startDate` 和 `endDate` 参数
- **THEN** 系统仅返回运出日期在指定范围内的记录

#### Scenario: 按废渣种类筛选

- **WHEN** 用户请求列表并指定 `wasteType` 参数
- **THEN** 系统仅返回废渣种类包含该关键词的记录（模糊匹配）

#### Scenario: 无权限

- **WHEN** 没有 `waste:read` 权限的用户尝试查询列表
- **THEN** 系统返回错误码 `ACCESS_DENIED`

### Requirement: 查看建筑废渣运出记录详情

系统 SHALL 允许有 `waste:read` 权限的用户查看单条废渣运出记录的完整信息，包含关联的照片文件列表。

#### Scenario: 成功查看详情

- **WHEN** 有权限的用户请求 `GET /api/construction-wastes/{id}`
- **THEN** 系统返回该记录的完整信息，包括所有关联字段和已上传照片的文件 ID 列表（含原始文件名和访问 URL）

#### Scenario: 记录已被软删除

- **WHEN** 用户请求一条已被软删除的记录
- **THEN** 系统返回错误码 `WASTE_NOT_FOUND`

#### Scenario: 记录不存在

- **WHEN** 用户请求一个不存在的记录 ID
- **THEN** 系统返回错误码 `WASTE_NOT_FOUND`

### Requirement: 删除建筑废渣运出记录

系统 SHALL 允许有 `waste:delete` 权限的用户软删除废渣运出记录，同时删除关联的文件。

#### Scenario: 成功软删除记录

- **WHEN** 有权限的用户请求 `DELETE /api/construction-wastes/{id}`
- **THEN** 系统将该记录的 `is_deleted` 标记为 1，并物理删除关联的所有上传文件和文件记录

#### Scenario: 无权限

- **WHEN** 没有 `waste:delete` 权限的用户尝试删除记录
- **THEN** 系统返回错误码 `ACCESS_DENIED`
