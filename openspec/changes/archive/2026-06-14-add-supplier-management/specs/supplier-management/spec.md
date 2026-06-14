## ADDED Requirements

### Requirement: 管理员查询供应商列表
系统 SHALL 允许具有 `supplier:read` 权限的用户分页查询供应商列表，支持按公司名称和状态筛选。

#### Scenario: 分页查询供应商列表
- **WHEN** 管理员请求 `GET /api/suppliers?page=1&size=20`
- **THEN** 系统返回第 1 页供应商数据，包含总记录数、分页信息和每条供应商的 ID、公司名称、联系人、联系电话、状态、创建时间

#### Scenario: 按公司名称搜索
- **WHEN** 管理员请求 `GET /api/suppliers?keyword=建材&page=1&size=20`
- **THEN** 系统返回公司名称包含"建材"的供应商列表

#### Scenario: 按状态筛选
- **WHEN** 管理员请求 `GET /api/suppliers?status=1&page=1&size=20`
- **THEN** 系统返回状态为"正常"的供应商列表

#### Scenario: 无权限用户被拒绝
- **WHEN** 无 `supplier:read` 权限的用户请求供应商列表
- **THEN** 系统返回 `supplier:read` 权限错误

### Requirement: 管理员查看供应商详情
系统 SHALL 允许具有 `supplier:read` 权限的用户查看单个供应商的完整信息。

#### Scenario: 查看存在且未删除的供应商
- **WHEN** 管理员请求 `GET /api/suppliers/{id}`，该供应商存在且未被删除
- **THEN** 系统返回供应商完整信息，包含公司名称、统一社会信用代码、联系人、联系电话、地址、状态、备注及关联的用户信息

#### Scenario: 查看已删除的供应商
- **WHEN** 管理员请求 `GET /api/suppliers/{id}`，该供应商已被软删除
- **THEN** 系统返回供应商不存在错误

### Requirement: 管理员创建供应商
系统 SHALL 允许具有 `supplier:create` 权限的用户手动创建供应商及其关联用户账号。

#### Scenario: 成功创建供应商
- **WHEN** 管理员提交 `POST /api/suppliers`，包含公司名称、联系人、联系电话及用户名和密码
- **THEN** 系统创建 `t_user` 记录（分配 supplier 角色）、创建 `t_supplier` 记录并关联，返回创建成功的供应商信息

#### Scenario: 公司名称为空
- **WHEN** 管理员提交的请求中公司名称为空
- **THEN** 系统返回参数校验错误"公司名称不能为空"

### Requirement: 管理员编辑供应商
系统 SHALL 允许具有 `supplier:update` 权限的用户编辑供应商信息。

#### Scenario: 成功编辑供应商
- **WHEN** 管理员提交 `PUT /api/suppliers/{id}`，包含新的公司名称、联系人、联系电话等
- **THEN** 系统更新 `t_supplier` 记录，返回更新后的供应商信息

#### Scenario: 编辑不存在的供应商
- **WHEN** 管理员请求编辑一个不存在的供应商 ID
- **THEN** 系统返回供应商不存在错误

### Requirement: 管理员删除供应商
系统 SHALL 允许具有 `supplier:delete` 权限的用户软删除供应商。

#### Scenario: 成功软删除
- **WHEN** 管理员请求 `DELETE /api/suppliers/{id}`
- **THEN** 系统将该供应商标记为已删除（`is_deleted=1`），同时禁用关联用户（`status=0`）

#### Scenario: 重复删除
- **WHEN** 管理员请求删除一个已删除的供应商
- **THEN** 系统返回供应商不存在错误
