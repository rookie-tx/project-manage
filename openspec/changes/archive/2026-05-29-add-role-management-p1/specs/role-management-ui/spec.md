## ADDED Requirements

### Requirement: 角色管理页面
系统 SHALL 在 `/roles` 路由提供角色管理页面，以表格形式展示角色列表，支持创建、编辑、删除操作。

#### Scenario: 显示角色列表
- **WHEN** 拥有 `role:read` 权限的用户导航到 `/roles`
- **THEN** 页面显示角色表格，列包含：ID、编码、名称、描述、是否受保护（是/否）、操作按钮

#### Scenario: 创建角色弹窗
- **WHEN** 用户点击"创建角色"按钮
- **THEN** 弹出模态对话框，包含：编码输入框、名称输入框、描述输入框、权限复选框分组（按资源分组）

#### Scenario: 编辑角色弹窗
- **WHEN** 用户点击某角色行的"编辑"按钮
- **THEN** 弹出模态对话框，预填角色信息并预选当前权限

#### Scenario: 删除非保护角色
- **WHEN** 用户点击非保护角色行的"删除"按钮
- **THEN** 弹出确认对话框"确认删除角色「XXX」？"，确认后删除并刷新列表

#### Scenario: 删除按钮对保护角色禁用
- **WHEN** 角色 isProtected 为 true
- **THEN** "删除"按钮置灰不可点击，tooltip 提示"系统内置角色不可删除"

#### Scenario: 无权限访问
- **WHEN** 没有 `role:read` 权限的用户导航到 `/roles`
- **THEN** 路由守卫重定向到 `/dashboard`
