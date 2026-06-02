## ADDED Requirements

### Requirement: 创建临时工（联动创建用户）
系统 SHALL 在 `POST /api/temp-workers` 端点创建临时工，同时自动创建 `t_user` 账户（username=手机号，初始密码 123456，角色 temp_worker）并标记首次登录需改密。

#### Scenario: 成功创建
- **WHEN** 管理员提交 `{ "projectId": 1, "phone": "13800138000", "workerType": "电工", "entryDate": "2026-06-01" }`
- **THEN** 系统创建 t_user（username=13800138000，must_change_password=1，role=temp_worker），再创建 t_temp_worker 关联

#### Scenario: 手机号已被注册
- **WHEN** 手机号已存在于 t_user
- **THEN** 系统返回错误提示"该手机号已被注册"

### Requirement: 按项目查询
系统 SHALL 在 `GET /api/projects/{id}/temp-workers` 返回该项目下所有未删除的临时工。

### Requirement: 编辑临时工
系统 SHALL 在 `PUT /api/temp-workers/{id}` 端点编辑工种和入职日期。

### Requirement: 离职处理
系统 SHALL 在设置离职日期时自动将 `t_user.status` 设为 DISABLED。

#### Scenario: 标记离职
- **WHEN** 管理员编辑临时工，设置 exitDate
- **THEN** 临时工状态变为离职，关联的 t_user 被禁用

### Requirement: 首次登录强制改密
系统 SHALL 在 `GET /api/auth/me` 返回 `mustChangePassword` 字段。前端检测为 true 时跳转修改密码页面。改密成功后清除标记。

#### Scenario: 临时工首次登录
- **WHEN** 临时工用手机号登录成功
- **THEN** GET /auth/me 返回 mustChangePassword=true，前端强制跳转设置页

#### Scenario: 改密后清除
- **WHEN** 临时工成功修改密码
- **THEN** t_user.must_change_password 更新为 0
