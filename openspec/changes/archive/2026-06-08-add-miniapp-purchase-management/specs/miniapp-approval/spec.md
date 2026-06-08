## MODIFIED Requirements

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
