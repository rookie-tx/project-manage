## ADDED Requirements

### Requirement: 采购申请列表

小程序 SHALL 提供采购申请列表页面，支持按项目筛选、按状态显示不同操作按钮、上拉加载更多和下拉刷新。

#### Scenario: 首次加载列表

- **WHEN** 已登录用户从小程序 Dashboard 点击"采购申请"入口进入列表页
- **THEN** 页面调用 `GET /api/purchase-requests?page=1&size=20`，以卡片形式展示采购申请列表，每条显示物料名称、项目名称、数量、单位、到场时间、状态标签

#### Scenario: 按项目筛选

- **WHEN** 用户在列表页的项目 picker 中选择某个项目
- **THEN** 页面重新请求携带 `projectId` 参数，仅展示该项目的采购申请，页码重置为 1

#### Scenario: 上拉加载更多

- **WHEN** 用户滚动到列表底部且还有更多数据
- **THEN** 页面请求下一页数据，将新记录追加到列表末尾

#### Scenario: 下拉刷新

- **WHEN** 用户在列表顶部下拉触发刷新
- **THEN** 页面清空已有列表，重新从 page=1 请求数据

#### Scenario: 待提交状态的操作按钮

- **WHEN** 采购申请状态为"待提交"（statusCode=0）
- **THEN** 卡片底部显示"提交审批""编辑""删除"三个操作按钮

#### Scenario: 非待提交状态的操作按钮

- **WHEN** 采购申请状态为"审批中""已通过"或"已驳回"
- **THEN** 卡片底部仅显示"查看"按钮

#### Scenario: 无数据

- **WHEN** 列表请求返回空数组
- **THEN** 页面显示"暂无采购申请"的空状态提示

### Requirement: 新建/编辑采购申请

小程序 SHALL 提供采购申请表单页面，通过 `id` 参数区分新建和编辑模式。

#### Scenario: 新建模式

- **WHEN** 用户从列表页点击"新建"按钮进入 `/pages/purchase/form`（无 `id` 参数）
- **THEN** 页面展示空白表单（项目、物料名称、规格、品牌、数量、单位、到场时间、备注），标题为"新建采购申请"，提交时调用 `POST /api/purchase-requests`

#### Scenario: 编辑模式

- **WHEN** 用户从列表或详情页点击"编辑"按钮进入 `/pages/purchase/form?id=123`
- **THEN** 页面调用 `GET /api/purchase-requests/123` 加载已有数据并预填到表单，标题为"编辑采购申请"，提交时调用 `PUT /api/purchase-requests/123`

#### Scenario: 编辑模式下非待提交状态

- **WHEN** 用户通过 URL 直接进入编辑页但采购申请状态不是"待提交"
- **THEN** 页面显示"当前状态不可编辑"提示，重定向回列表页

#### Scenario: 项目选择

- **WHEN** 用户点击"所属项目"选择器
- **THEN** 弹出项目名称列表（数据来自 `GET /api/projects/select`），用户选择后显示所选项目名称

#### Scenario: 单位选择

- **WHEN** 用户点击"单位"选择器
- **THEN** 弹出单位列表（个、台、套、米、吨、立方米、卷、桶、箱、其他），用户选择后显示对应单位名称

#### Scenario: 必填校验

- **WHEN** 用户提交表单但缺少项目、物料名称、数量、单位或到场时间
- **THEN** 页面显示对应的校验错误提示，不发起提交请求

#### Scenario: 成功提交

- **WHEN** 用户填写完所有必填字段并点击提交
- **THEN** 调用对应 API，成功后显示 Toast，返回上一页并通知列表页刷新

### Requirement: 采购申请详情

小程序 SHALL 提供采购申请详情页，展示完整信息和审批功能。

#### Scenario: 成功加载详情

- **WHEN** 用户从列表页点击进入详情页
- **THEN** 页面调用 `GET /api/purchase-requests/{id}`，展示项目名称、物料名称、规格、品牌、数量、单位、到场时间、状态、备注、创建人、创建时间、更新时间

#### Scenario: 审批历史时间线

- **WHEN** 详情页加载时
- **THEN** 页面同时调用 `GET /api/tasks/history?businessType=PURCHASE_REQUEST&businessId=X`，在底部以时间线形式展示审批历史

#### Scenario: 提交审批区（待提交状态）

- **WHEN** 采购申请状态为"待提交"
- **THEN** 详情页展示审批人选区域（部门经理、财务审批、总经理三个 picker，数据来自 `GET /api/users/select`）和"确认提交"按钮

#### Scenario: 提交审批成功

- **WHEN** 用户选择审批人并点击"确认提交"
- **THEN** 调用 `POST /api/tasks/start` 携带 businessType、businessId 和审批人变量，成功后刷新详情页

#### Scenario: 至少选择一个审批人

- **WHEN** 用户未选择任何审批人即点击"确认提交"
- **THEN** 页面提示"请至少选择一个审批人"，不发起请求

#### Scenario: 编辑和删除按钮（待提交状态）

- **WHEN** 采购申请状态为"待提交"
- **THEN** 详情页底部显示"编辑"和"删除"按钮

#### Scenario: 非待提交状态隐藏操作按钮

- **WHEN** 采购申请状态不是"待提交"
- **THEN** 详情页不显示编辑、删除按钮和提交审批区

#### Scenario: 删除确认

- **WHEN** 用户点击"删除"按钮
- **THEN** 弹出确认弹窗，确认后调用 `DELETE /api/purchase-requests/{id}`，成功后返回列表页并触发刷新

### Requirement: Dashboard 采购申请入口

小程序 Dashboard SHALL 新增采购申请功能入口。

#### Scenario: 点击采购申请入口

- **WHEN** 已登录用户在 Dashboard 功能入口网格中点击"采购申请"
- **THEN** 跳转到采购申请列表页 `/pages/purchase/list`

#### Scenario: 入口图标和文字

- **WHEN** 用户查看 Dashboard 功能入口网格
- **THEN** 采购申请入口显示图标"📦"、名称"采购申请"、提示文字"点击进入"
