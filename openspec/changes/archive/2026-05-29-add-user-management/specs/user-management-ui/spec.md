## ADDED Requirements

### Requirement: 用户列表页面
系统 SHALL 在 `/users` 路由提供用户管理列表页面，以表格形式展示分页用户数据。

#### Scenario: 显示用户列表
- **WHEN** 管理员导航到 `/users`
- **THEN** 页面显示用户表格，列包含：ID、用户名、状态（标签样式，ACTIVE 绿色/DISABLED 灰色）、角色（标签列表）、创建时间、操作（编辑/删除按钮）

#### Scenario: 分页浏览
- **WHEN** 用户总数超过单页条数
- **THEN** 表格底部显示分页器，可切换页码和每页条数

#### Scenario: 搜索用户
- **WHEN** 管理员在搜索框输入关键字并触发搜索
- **THEN** 表格刷新为匹配的用户列表

#### Scenario: 按状态筛选
- **WHEN** 管理员选择状态筛选条件（全部/ACTIVE/DISABLED）
- **THEN** 表格刷新为对应状态的用户列表

#### Scenario: 普通用户访问被拒绝
- **WHEN** 非管理员用户导航到 `/users`
- **THEN** 页面显示无权访问提示或重定向到 Dashboard

### Requirement: 创建用户页面
系统 SHALL 在 `/users/new` 路由提供创建用户表单。

#### Scenario: 创建用户表单
- **WHEN** 管理员导航到 `/users/new`
- **THEN** 页面显示表单包含：用户名输入框、密码输入框、角色多选下拉框（可选）

#### Scenario: 成功创建用户
- **WHEN** 管理员填写有效用户名和密码并提交
- **THEN** 系统创建用户，跳转回用户列表页

#### Scenario: 用户名重复
- **WHEN** 管理员填写的用户名已存在
- **THEN** 表单显示错误提示"用户名已存在"

### Requirement: 编辑用户页面
系统 SHALL 在 `/users/:id/edit` 路由提供编辑用户表单，支持修改状态和重新分配角色。

#### Scenario: 编辑用户表单
- **WHEN** 管理员导航到 `/users/:id/edit`
- **THEN** 页面预填当前用户名（只读）、状态（ACTIVE/DISABLED 单选）、角色（多选，预选当前角色）

#### Scenario: 成功编辑用户
- **WHEN** 管理员修改状态或角色并提交
- **THEN** 系统更新用户信息，跳转回用户列表页

### Requirement: 用户列表操作按钮
系统 SHALL 在用户列表每行提供编辑和删除操作。

#### Scenario: 点击编辑
- **WHEN** 管理员点击某行的"编辑"按钮
- **THEN** 页面跳转到 `/users/:id/edit`

#### Scenario: 点击删除
- **WHEN** 管理员点击某行的"删除"按钮
- **THEN** 弹出确认对话框"确认删除用户 XXX？"，确认后执行软删除并刷新列表

#### Scenario: 删除成功提示
- **WHEN** 用户软删除成功
- **THEN** 列表显示提示"用户已删除"，表格刷新
