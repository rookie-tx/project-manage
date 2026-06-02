## MODIFIED Requirements

### Requirement: 查询待办任务列表
系统 SHALL 在 `GET /api/tasks` 端点返回当前用户的审批待办列表，返回通用字段而非业务专属字段。

#### Scenario: 查询待办
- **WHEN** 用户请求 `GET /api/tasks?page=1&size=20`
- **THEN** 系统返回当前用户的待办任务列表，每条包含 taskId、businessType、businessId、businessTitle（业务标题，如采购申请名称）、businessSubtitle（业务副标题，如项目名称）、nodeName、createTime，按创建时间倒序。展示信息从流程变量中读取，不直接查询业务表。

#### Scenario: 无待办任务
- **WHEN** 当前用户没有待办的审批任务
- **THEN** 系统返回空列表

### Requirement: 审批通过
系统 SHALL 在 `POST /api/tasks/{taskId}/complete` 端点完成审批任务，通过 BusinessTaskCallback 回调通知业务模块。

#### Scenario: 审批通过
- **WHEN** 用户对任务调用完成接口，传入 `approved: true`
- **THEN** 系统完成 Flowable 任务，设置流程变量 `approvalResult=APPROVED`，流程流转至下一节点。若流程结束，通过 BusinessTaskCallback.onTaskCompleted() 回调业务模块

#### Scenario: 审批驳回
- **WHEN** 用户对任务调用完成接口，传入 `approved: false`
- **THEN** 系统完成 Flowable 任务，设置流程变量 `approvalResult=REJECTED`，流程流转至驳回结束节点，通过 BusinessTaskCallback.onTaskCompleted() 回调业务模块

#### Scenario: 任务不属于当前用户
- **WHEN** 当前用户不是任务的 assignee
- **THEN** 系统返回 BusinessException，错误码 6002

### Requirement: 查询审批历史
系统 SHALL 在 `GET /api/tasks/history` 端点返回审批记录，接受 businessType 和 businessId 参数。

#### Scenario: 有审批记录
- **WHEN** 请求 `GET /api/tasks/history?businessType=PURCHASE_REQUEST&businessId=123` 且该业务已提交审批
- **THEN** 系统通过 businessType + businessId 查找 WorkflowInstance 获取 processInstanceId，查询 Flowable 历史任务表，返回各审批节点的 nodeName、assignee（审批人）、result（通过/驳回）、comment（审批意见）、time（审批时间）

#### Scenario: 未提交审批
- **WHEN** 指定业务尚未启动审批流程
- **THEN** 系统返回空列表

## REMOVED Requirements

### Requirement: 提交审批
**Reason**: 启动流程的逻辑移至 task 模块 `startProcess()`，业务模块 submit 端点内部调用 task 模块，不再直接操作 Flowable API
**Migration**: 采购申请提交审批的 API 端点 `POST /api/purchase-requests/{id}/submit` 保留，内部改为调用 `TaskApplicationService.startProcess(BusinessType.PURCHASE_REQUEST, id, variables)`
