## ADDED Requirements

### Requirement: 查询角色列表
系统 SHALL 在 `GET /api/roles` 端点返回所有角色的列表，包含 id、code、name、description、isProtected 字段。

#### Scenario: 查询所有角色
- **WHEN** 管理员请求 `GET /api/roles`
- **THEN** 系统返回角色列表，包含 admin、project_manager、finance、user 四个预置角色

### Requirement: 查询所有权限
系统 SHALL 在 `GET /api/permissions` 端点返回所有已注册权限的列表。

#### Scenario: 查询权限列表
- **WHEN** 管理员请求 `GET /api/permissions`
- **THEN** 系统返回至少 15 条权限记录，每条包含 id、code、name、description

### Requirement: 查询角色权限
系统 SHALL 在 `GET /api/roles/{id}/permissions` 端点返回指定角色当前关联的权限 ID 列表。

#### Scenario: 查询 admin 角色权限
- **WHEN** 管理员请求 `GET /api/roles/1/permissions`
- **THEN** 系统返回该角色的所有权限 ID 列表

#### Scenario: 角色不存在
- **WHEN** 管理员请求不存在的角色 ID
- **THEN** 系统返回 `code: 7001`

### Requirement: 创建角色
系统 SHALL 在 `POST /api/roles` 端点允许管理员创建新角色。

#### Scenario: 成功创建角色
- **WHEN** 管理员请求 `POST /api/roles`，请求体为 `{ "code": "auditor", "name": "审计员", "description": "审计相关权限", "permissionIds": [1, 8, 12] }`
- **THEN** 系统创建角色，返回角色详情

#### Scenario: 角色 code 重复
- **WHEN** 管理员创建 code 已存在的角色
- **THEN** 系统返回错误提示"角色编码已存在"

### Requirement: 编辑角色
系统 SHALL 在 `PUT /api/roles/{id}` 端点允许管理员编辑角色信息和权限。

#### Scenario: 成功编辑角色
- **WHEN** 管理员请求 `PUT /api/roles/{id}`，请求体为 `{ "name": "财务主管", "permissionIds": [1, 8, 11, 12, 13] }`
- **THEN** 系统更新角色名称并替换权限关联

### Requirement: 删除角色
系统 SHALL 在 `DELETE /api/roles/{id}` 端点允许管理员删除非保护角色。

#### Scenario: 成功删除非保护角色
- **WHEN** 管理员删除 is_protected=0 的角色
- **THEN** 系统删除该角色及关联数据

#### Scenario: 拒绝删除受保护角色
- **WHEN** 管理员尝试删除 admin 角色
- **THEN** 系统返回错误提示"系统内置角色不可删除"

#### Scenario: 角色不存在
- **WHEN** 管理员删除不存在的角色
- **THEN** 系统返回 `code: 7001`
