## ADDED Requirements

### Requirement: 角色数据模型
系统 SHALL 在 `t_role` 表中存储角色信息，包含 code（唯一标识）、name（中文名称）和 description（描述）。

#### Scenario: 查询所有角色
- **WHEN** 系统查询 `t_role` 表
- **THEN** 返回至少 4 条预置角色记录：admin、project_manager、finance、user

#### Scenario: 角色 code 唯一
- **WHEN** 尝试插入重复 code 的角色
- **THEN** 数据库拒绝插入并抛出唯一约束异常

### Requirement: 权限数据模型
系统 SHALL 在 `t_permission` 表中存储权限信息，code 字段采用 `资源:操作` 命名格式。

#### Scenario: 查询所有权限
- **WHEN** 系统查询 `t_permission` 表
- **THEN** 返回预置权限列表，包含 user:read、user:create、user:update、user:delete、user:role:assign、role:read、project:read、project:create、project:update、project:delete、pr:read、pr:create、pr:approve、task:read、task:complete

#### Scenario: 权限 code 格式
- **WHEN** 系统存储权限 code
- **THEN** 格式为 `<资源>:<操作>`，如 `user:create`、`project:read`

### Requirement: 用户-角色多对多关联
系统 SHALL 在 `t_user_role` 表中维护用户与角色的多对多关系，同一用户可拥有多个角色。

#### Scenario: 用户关联多个角色
- **WHEN** 为用户分配 role_id=2 和 role_id=3
- **THEN** `t_user_role` 表中插入两条记录，user_id 相同，role_id 分别为 2 和 3

#### Scenario: 用户-角色唯一约束
- **WHEN** 尝试为同一用户重复分配同一角色
- **THEN** 数据库拒绝插入并抛出唯一约束异常

### Requirement: 角色-权限多对多关联
系统 SHALL 在 `t_role_permission` 表中维护角色与权限的多对多关系。

#### Scenario: 管理员角色拥有所有权限
- **WHEN** 查询 admin 角色的权限
- **THEN** 返回全部 15 条权限记录

#### Scenario: 普通用户角色权限受限
- **WHEN** 查询 user 角色的权限
- **THEN** 仅返回 user:read、project:read、pr:read、task:read 四条权限

### Requirement: LoginContext 携带角色和权限
系统 SHALL 在认证拦截器中加载当前用户的角色 code 列表和权限 code 列表，并填充到 LoginContext 中。

#### Scenario: 认证用户获取角色和权限
- **WHEN** 用户 admin（拥有 admin 角色）发起请求
- **THEN** LoginContext 包含 `roles: ["admin"]` 和 `permissions: ["user:read", "user:create", ...]`（全部权限）

#### Scenario: 无角色用户
- **WHEN** 用户未被分配任何角色
- **THEN** LoginContext 包含 `roles: []` 和 `permissions: []`

### Requirement: 预置种子数据
系统 SHALL 在数据库初始化脚本中预置四种角色和对应权限关联。

#### Scenario: 角色预置数据
- **WHEN** 执行 schema.sql
- **THEN** `t_role` 表包含 admin（管理员）、project_manager（项目经理）、finance（财务）、user（普通用户）

#### Scenario: 权限预置数据
- **WHEN** 执行 schema.sql
- **THEN** `t_permission` 表包含至少 15 条权限记录

#### Scenario: 角色-权限预置关联
- **WHEN** 执行 schema.sql
- **THEN** admin 角色关联全部权限，project_manager/finance/user 按设计各有关联权限
