## ADDED Requirements

### Requirement: 项目列表

小程序 SHALL 提供项目列表页面，支持关键词搜索、类型筛选、状态筛选、上拉加载更多和下拉刷新。

#### Scenario: 首次加载列表

- **WHEN** 已登录用户从小程序 Dashboard 点击"项目管理"入口进入列表页
- **THEN** 页面调用 `GET /api/projects?page=1&size=20`，以卡片形式展示项目列表，每条显示项目编号、名称、类型、状态、甲方名称、项目经理

#### Scenario: 关键词搜索

- **WHEN** 用户在搜索框输入关键词并点击搜索
- **THEN** 页面重新请求携带 `keyword` 参数，按项目名称模糊匹配，页码重置为 1

#### Scenario: 按类型筛选

- **WHEN** 用户在类型 chip 中选择某个类型（如"房建"）
- **THEN** 页面重新请求携带 `type` 参数，仅展示该类型的项目

#### Scenario: 按状态筛选

- **WHEN** 用户在状态 chip 中选择某个状态（如"在建"）
- **THEN** 页面重新请求携带 `status` 参数，仅展示该状态的项目

#### Scenario: 上拉加载更多

- **WHEN** 用户滚动到列表底部且还有更多数据
- **THEN** 页面请求下一页数据，将新记录追加到列表末尾

#### Scenario: 下拉刷新

- **WHEN** 用户在列表顶部下拉触发刷新
- **THEN** 页面清空已有列表，重新从 page=1 请求数据

#### Scenario: 点击项目进入详情

- **WHEN** 用户点击某个项目卡片
- **THEN** 页面跳转到 `/pages/project/detail?id=<项目ID>`

#### Scenario: 无数据

- **WHEN** 列表请求返回空数组
- **THEN** 页面显示"暂无项目"的空状态提示

### Requirement: 新建/编辑项目

小程序 SHALL 提供项目表单页面，通过 `id` 参数区分新建和编辑模式。

#### Scenario: 新建模式

- **WHEN** 拥有 `project:create` 权限的用户点击列表页"新建"按钮进入表单页
- **THEN** 页面展示空白表单，提交时调用 `POST /api/projects`

#### Scenario: 编辑模式

- **WHEN** 拥有 `project:update` 权限的用户点击编辑按钮进入 `/pages/project/form?id=123`
- **THEN** 页面调用 `GET /api/projects/123` 加载已有数据并预填到表单，提交时调用 `PUT /api/projects/123`

#### Scenario: 项目类型选择

- **WHEN** 用户点击"项目类型"选择器
- **THEN** 弹出类型列表（房建/市政/装修/路桥/水利/其他）

#### Scenario: 项目经理选择

- **WHEN** 用户点击"项目经理"选择器
- **THEN** 弹出用户列表（数据来自 `GET /api/users/select`）

#### Scenario: 必填校验

- **WHEN** 用户提交表单但缺少项目名称、类型、项目经理或计划日期
- **THEN** 页面显示对应的校验错误提示

#### Scenario: 成功提交

- **WHEN** 用户填写完必填字段并提交
- **THEN** 调用对应 API，成功后显示 Toast，返回上一页并通知列表页刷新

### Requirement: 项目详情

小程序 SHALL 提供项目详情页，展示完整信息和操作按钮。

#### Scenario: 成功加载详情

- **WHEN** 用户从列表页点击进入详情页
- **THEN** 页面调用 `GET /api/projects/{id}`，展示项目编号、名称、类型、地点、甲方、监理、项目经理、计划日期、实际日期、状态、备注、创建时间

#### Scenario: 编辑和删除按钮

- **WHEN** 用户查看项目详情
- **THEN** 底部显示操作按钮：拥有 `project:update` 权限时显示"编辑"，拥有 `project:delete` 权限时显示"删除"

#### Scenario: 删除确认

- **WHEN** 用户点击"删除"按钮
- **THEN** 弹出确认弹窗，确认后调用 `DELETE /api/projects/{id}`，成功后返回列表页并触发刷新

### Requirement: Dashboard 项目管理入口激活

小程序 Dashboard SHALL 将项目管理功能入口从"即将上线"占位状态激活。

#### Scenario: 点击项目管理入口

- **WHEN** 已登录用户在 Dashboard 功能入口网格中点击"项目管理"
- **THEN** 跳转到项目列表页 `/pages/project/list`
