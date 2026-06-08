## ADDED Requirements

### Requirement: 待办任务列表

小程序 SHALL 提供待办审批任务列表页面，展示当前用户的待办任务卡片，支持上拉加载更多和下拉刷新。

#### Scenario: 首次加载待办列表

- **WHEN** 已登录用户从小程序 Dashboard 点击"待办审批"入口进入列表页
- **THEN** 页面调用 `GET /api/tasks?page=1&size=20`，以卡片形式展示待办任务列表，每条显示业务标题（businessTitle）、业务副标题（businessSubtitle）、当前审批节点（nodeName）、到达时间（createTime）

#### Scenario: 上拉加载更多

- **WHEN** 用户滚动到列表底部且还有更多数据
- **THEN** 页面请求下一页数据（page 递增），将新记录追加到列表末尾

#### Scenario: 下拉刷新

- **WHEN** 用户在列表顶部下拉触发刷新
- **THEN** 页面清空已有列表，重新从 page=1 请求数据，加载完成后停止刷新动画

#### Scenario: 无待办任务

- **WHEN** 当前用户没有待办的审批任务
- **THEN** 页面显示"暂无待办任务"的空状态提示

#### Scenario: 点击任务进入审批

- **WHEN** 用户点击某个待办任务卡片
- **THEN** 页面跳转到 `/pages/approval/action`，通过 URL 参数传递 taskId、businessType、businessId、businessTitle、businessSubtitle、nodeName

### Requirement: 审批操作

小程序 SHALL 提供审批操作页面，允许有 `task:complete` 权限的用户查看任务信息、填写审批意见并做出通过或驳回决定。当任务的业务类型为 `PURCHASE_REQUEST` 时，页面提供跳转到采购申请详情页的链接。

#### Scenario: 有权限用户查看审批页

- **WHEN** 拥有 `task:complete` 权限的用户从列表页进入审批操作页
- **THEN** 页面展示任务信息（业务类型、业务标题、业务副标题、当前节点、到达时间），底部显示"通过"和"驳回"两个按钮。若 `businessType` 为 `PURCHASE_REQUEST`，额外显示"查看采购详情"链接

#### Scenario: 无权限用户查看审批页

- **WHEN** 登录用户没有 `task:complete` 权限
- **THEN** 页面仍展示任务信息和审批历史，但不显示"通过"和"驳回"按钮。若 `businessType` 为 `PURCHASE_REQUEST`，仍显示"查看采购详情"链接

#### Scenario: 点击查看采购详情

- **WHEN** 用户点击"查看采购详情"链接
- **THEN** 跳转到 `/pages/purchase/detail?id=<businessId>`

#### Scenario: 审批通过

- **WHEN** 用户填写审批意见（可选）并点击"通过"按钮
- **THEN** 调用 `POST /api/tasks/{taskId}/complete` 传入 `{"approved": true, "comment": "..."}`，成功后显示"审批通过"Toast，返回列表页并触发列表刷新

#### Scenario: 审批驳回

- **WHEN** 用户填写驳回原因并点击"驳回"按钮
- **THEN** 调用 `POST /api/tasks/{taskId}/complete` 传入 `{"approved": false, "comment": "..."}`，成功后显示"已驳回"Toast，返回列表页并触发列表刷新

#### Scenario: 驳回时意见为空

- **WHEN** 用户点击"驳回"但未填写审批意见
- **THEN** 页面提示"驳回须填写审批意见"，不发起请求

#### Scenario: 通过时意见选填

- **WHEN** 用户点击"通过"且未填写审批意见
- **THEN** 系统正常提交（意见字段传 null 或空字符串），审批通过

#### Scenario: 网络错误处理

- **WHEN** 审批提交过程中网络请求失败
- **THEN** 页面显示错误提示，保持在审批操作页，保留已输入的意见内容

### Requirement: 审批历史时间线

小程序 SHALL 在审批操作页底部展示该业务的已完结审批记录时间线。

#### Scenario: 加载审批历史

- **WHEN** 审批操作页加载时
- **THEN** 页面调用 `GET /api/tasks/history?businessType=X&businessId=X`，在审批操作区下方以时间线形式展示各审批节点的 nodeName、assignee、result（通过/驳回）、comment、time

#### Scenario: 无审批记录

- **WHEN** 指定业务尚未有已完结的审批节点
- **THEN** 审批历史区域显示"暂无审批记录"

### Requirement: Dashboard 待办审批入口激活

小程序 Dashboard SHALL 将待办审批功能入口从"即将上线"占位状态激活为可跳转的有效入口，并显示待办任务数量角标。

#### Scenario: 点击待办审批入口

- **WHEN** 已登录用户在 Dashboard 功能入口网格中点击"待办审批"
- **THEN** 跳转到待办列表页 `/pages/approval/list`

#### Scenario: 显示待办数量角标

- **WHEN** Dashboard 页面显示时（onShow）
- **THEN** 调用 `GET /api/tasks/count`，若有待办任务则在"待办审批"卡片上显示红色数字角标

#### Scenario: 无待办任务时不显示角标

- **WHEN** 待办计数接口返回 count=0 或请求失败
- **THEN** 待办审批卡片不显示角标

#### Scenario: 入口图标和文字

- **WHEN** 用户查看 Dashboard 功能入口网格
- **THEN** 待办审批入口显示图标"✅"、名称"待办审批"、提示文字"点击进入"
