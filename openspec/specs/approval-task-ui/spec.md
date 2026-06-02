# approval-task-ui Specification

## Purpose
TBD - created by archiving change integrate-flowable-approval. Update Purpose after archive.
## Requirements
### Requirement: 审批待办任务页面
系统 SHALL 在 `/tasks` 路由提供当前用户的待办审批任务列表，展示通用业务字段。

#### Scenario: 查看待办任务
- **WHEN** 用户导航至 `/tasks`
- **THEN** 系统加载当前用户的待办任务列表，显示业务标题（businessTitle）、业务副标题（businessSubtitle）、当前审批节点（nodeName）、到达时间（createTime）

#### Scenario: 点击任务进入审批
- **WHEN** 用户点击某个待办任务
- **THEN** 系统展示审批对话框，根据任务的 businessType 动态加载业务摘要信息，包含审批意见输入框、通过/驳回按钮

#### Scenario: 审批通过
- **WHEN** 用户填写意见（可选）并点击"通过"
- **THEN** 系统调用审批通过接口，显示成功提示，刷新任务列表

#### Scenario: 审批驳回
- **WHEN** 用户填写驳回原因并点击"驳回"
- **THEN** 系统调用审批驳回接口，显示成功提示，刷新任务列表

### Requirement: 待办任务导航入口
系统 SHALL 在 Dashboard 顶栏新增"待办任务"菜单或徽章。

#### Scenario: 有未处理任务时
- **WHEN** 当前用户有待办审批任务
- **THEN** 菜单项显示任务数量徽章

#### Scenario: 点击待办任务菜单
- **WHEN** 用户点击"待办任务"
- **THEN** 系统导航至 `/tasks`

### Requirement: 审批历史时间线
系统 SHALL 通过 task 模块通用接口获取审批历史，不再通过业务模块专属端点。

#### Scenario: 已提交审批的申请
- **WHEN** 用户查看已提交审批的业务详情
- **THEN** 系统调用 `GET /api/tasks/history?businessType=xxx&businessId=xxx` 获取审批历史，在详情页底部展示审批时间线（纵向），每个节点显示审批人名称、审批时间、审批结果、审批意见

#### Scenario: 未提交审批的申请
- **WHEN** 用户查看状态为"待提交"的业务详情
- **THEN** 审批时间线区域为空或显示"尚未提交审批"

