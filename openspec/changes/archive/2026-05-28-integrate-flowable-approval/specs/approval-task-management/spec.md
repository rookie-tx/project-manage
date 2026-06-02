## ADDED Requirements

### Requirement: 提交审批
系统 SHALL 在 `POST /api/purchase-requests/{id}/submit` 端点启动审批流程。

#### Scenario: 成功提交
- **WHEN** 采购申请状态为"待提交"，用户调用提交审批接口，传入审批人 ID（部门经理、财务、总经理）
- **THEN** 系统启动 Flowable 流程实例，更新采购申请状态为"审批中"，记录 processInstanceId，返回 HTTP 200

#### Scenario: 非待提交状态不可提交
- **WHEN** 采购申请状态不是"待提交"
- **THEN** 系统返回 BusinessException，错误码 4003

#### Scenario: 采购申请不存在
- **WHEN** 请求的 ID 不存在
- **THEN** 系统返回 BusinessException，错误码 4002

### Requirement: 查询待办任务列表
系统 SHALL 在 `GET /api/tasks` 端点返回当前用户的审批待办列表。

#### Scenario: 查询待办
- **WHEN** 用户请求 `GET /api/tasks?page=1&size=20`
- **THEN** 系统返回当前用户的待办任务列表，包含任务 ID、采购申请信息（通过 purchaseRequestId 变量联查）、任务创建时间，按创建时间倒序

#### Scenario: 无待办任务
- **WHEN** 当前用户没有待办的审批任务
- **THEN** 系统返回空列表

### Requirement: 审批通过
系统 SHALL 在 `POST /api/tasks/{taskId}/complete` 端点完成审批任务。

#### Scenario: 审批通过
- **WHEN** 用户对任务调用完成接口，传入 `approved: true` 和审批意见
- **THEN** 系统完成任务，设置流程变量 `approvalResult=APPROVED`，流程流转至下一节点

#### Scenario: 审批驳回
- **WHEN** 用户对任务调用完成接口，传入 `approved: false` 和驳回理由
- **THEN** 系统完成任务，设置流程变量 `approvalResult=REJECTED`，流程流转至驳回结束节点，更新采购申请状态为"已驳回"

#### Scenario: 任务不属于当前用户
- **WHEN** 当前用户不是任务的 assignee
- **THEN** 系统返回 BusinessException，错误码 4004

### Requirement: 查询审批历史
系统 SHALL 在 `GET /api/purchase-requests/{id}/approval-history` 端点返回审批记录。

#### Scenario: 有审批记录
- **WHEN** 采购申请已提交审批
- **THEN** 系统通过 processInstanceId 查询 Flowable 历史任务表，返回各审批节点的审批人、审批时间、审批意见、审批结果

#### Scenario: 未提交审批
- **WHEN** 采购申请状态为"待提交"
- **THEN** 系统返回空审批历史列表

### Requirement: ErrorCode 扩展
系统 SHALL 在 4xxx 范围新增审批相关错误码。

| 错误码 | 含义 |
|--------|------|
| 4004 | 任务不属于当前用户 |
| 4005 | 审批流程启动失败 |
