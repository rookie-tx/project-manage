## ADDED Requirements

### Requirement: 供应商查看自己的资料
系统 SHALL 允许具有 `supplier:profile` 权限的供应商用户查看自己的企业资料。

#### Scenario: 查看自己的供应商资料
- **WHEN** 供应商用户请求 `GET /api/supplier/my-profile`
- **THEN** 系统根据当前登录用户的 ID 查找关联的 Supplier 记录，返回公司名称、统一社会信用代码、联系人、联系电话、地址、状态等信息

#### Scenario: 非供应商用户查看
- **WHEN** 非供应商用户（无 supplier 角色）请求 `GET /api/supplier/my-profile`
- **THEN** 系统返回权限错误 `supplier:profile`

#### Scenario: 供应商资料不存在
- **WHEN** 具有 supplier 角色的用户请求查看资料，但该用户无对应的 Supplier 记录
- **THEN** 系统返回供应商不存在错误

### Requirement: 供应商编辑自己的资料
系统 SHALL 允许具有 `supplier:profile` 权限的供应商用户编辑自己的企业资料。

#### Scenario: 成功编辑自己的资料
- **WHEN** 供应商用户提交 `PUT /api/supplier/my-profile`，包含更新后的联系人、联系电话、地址等
- **THEN** 系统更新该用户关联的 Supplier 记录，返回更新后的供应商信息

#### Scenario: 不允许修改公司名称
- **WHEN** 供应商用户尝试修改公司名称
- **THEN** 系统忽略公司名称变更（仅管理员可修改），其他字段正常更新

#### Scenario: 不允许修改统一社会信用代码
- **WHEN** 供应商用户尝试修改统一社会信用代码
- **THEN** 系统忽略该字段变更（仅管理员可修改），其他字段正常更新
