## ADDED Requirements

### Requirement: 采购申请列表页
系统 SHALL 在 `/procurement` 路由提供采购申请列表页，支持按项目筛选和分页。

#### Scenario: 首次进入列表页
- **WHEN** 用户导航至 `/procurement`
- **THEN** 系统加载第 1 页数据（每页 20 条），显示物料名称、项目名称、规格、品牌、数量、单位、到场时间、状态、操作按钮

#### Scenario: 按项目筛选
- **WHEN** 用户选择项目下拉中的某个项目
- **THEN** 系统只返回该项目的采购申请，页码重置为 1

#### Scenario: 分页切换
- **WHEN** 用户点击分页器
- **THEN** 系统加载对应页数据

### Requirement: 新建采购申请
系统 SHALL 在 `/procurement/new` 路由提供新建表单。

#### Scenario: 成功创建
- **WHEN** 用户填写完整必填字段（项目、名称、规格、品牌、数量、单位、到场时间）并提交
- **THEN** 系统创建成功，跳转至列表页

#### Scenario: 必填校验失败
- **WHEN** 用户未选择项目或未填写名称即提交
- **THEN** 系统在对应字段下方显示校验提示，不发起请求

#### Scenario: 取消新建
- **WHEN** 用户点击取消按钮
- **THEN** 系统返回列表页

### Requirement: 采购申请详情页
系统 SHALL 在 `/procurement/:id` 路由提供只读详情展示。

#### Scenario: 查看详情
- **WHEN** 用户导航至详情页
- **THEN** 系统展示所有字段：项目名称、物料名称、规格、品牌、数量、单位、到场时间、状态、备注、创建时间、更新时间

#### Scenario: 采购申请不存在
- **WHEN** 请求的 ID 不存在
- **THEN** 系统显示"采购申请不存在"的错误提示

### Requirement: 编辑采购申请
系统 SHALL 在 `/procurement/:id/edit` 路由提供编辑表单，表单预填当前值。

#### Scenario: 成功编辑
- **WHEN** 用户修改字段并提交
- **THEN** 系统更新成功，跳转回上一页

#### Scenario: 状态只读展示
- **WHEN** 用户进入编辑页
- **THEN** 系统展示当前状态的中文标签，状态不可修改

#### Scenario: 取消编辑
- **WHEN** 用户点击取消按钮
- **THEN** 系统返回上一页

### Requirement: 列表页操作按钮
系统 SHALL 在列表页每行提供查看、编辑、删除按钮。

#### Scenario: 点击删除
- **WHEN** 用户点击删除按钮并在确认弹窗中确认
- **THEN** 系统执行软删除并刷新列表

### Requirement: 项目下拉选择器
系统 SHALL 在新建和编辑表单中提供项目下拉选择器。

#### Scenario: 选择项目
- **WHEN** 用户点击项目下拉框
- **THEN** 系统加载全部项目列表（项目编号 + 项目名称），支持搜索过滤

### Requirement: 导航菜单入口
系统 SHALL 在 Dashboard 顶栏菜单新增"采购申请"菜单项。

#### Scenario: 点击采购申请菜单
- **WHEN** 用户点击"采购申请"
- **THEN** 系统导航至 `/procurement`

### Requirement: 路由认证守卫
采购申请所有路由 SHALL 要求用户认证。

#### Scenario: 未登录访问
- **WHEN** 未登录用户导航至 `/procurement`
- **THEN** 系统重定向至 `/login`
