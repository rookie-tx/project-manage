## ADDED Requirements

### Requirement: 通用启动流程
系统 SHALL 通过 `TaskApplicationService.startProcess(businessType, businessId, variables)` 启动审批流程。

#### Scenario: 成功启动流程
- **WHEN** 业务模块调用 startProcess 传入有效的 businessType、businessId 和流程变量
- **THEN** 系统根据 businessType 获取对应的 processDefinitionKey，将 businessType、businessId、businessTitle、businessSubtitle 自动注入流程变量，启动 Flowable 流程实例，保存 WorkflowInstance 记录，状态为"运行中"

#### Scenario: 同一业务重复启动
- **WHEN** 同一 businessType + businessId 已存在运行中的流程实例
- **THEN** 系统返回 BusinessException，错误码 6xxx

#### Scenario: 未知业务类型
- **WHEN** 传入的 businessType 未注册对应的 BusinessTaskCallback
- **THEN** 系统返回 BusinessException，错误码 6xxx

### Requirement: 通用查询待办任务列表
系统 SHALL 在 `GET /api/tasks` 端点返回当前用户的审批待办列表，使用通用字段。

#### Scenario: 查询待办
- **WHEN** 用户请求 `GET /api/tasks?page=1&size=20`
- **THEN** 系统返回当前用户的待办任务列表，每条包含 taskId、businessType、businessId、businessTitle、businessSubtitle、nodeName、createTime

#### Scenario: 多种业务类型混合展示
- **WHEN** 当前用户同时有待审批的采购申请任务和项目审批任务
- **THEN** 系统在同一列表中返回所有类型的待办任务，通过 businessType 字段区分

#### Scenario: 无待办任务
- **WHEN** 当前用户没有待办的审批任务
- **THEN** 系统返回空列表

### Requirement: 通用完成任务
系统 SHALL 在 `POST /api/tasks/{taskId}/complete` 端点完成审批任务，并通过回调通知业务模块。

#### Scenario: 审批通过
- **WHEN** 用户对任务调用完成接口，传入 `approved: true`
- **THEN** 系统完成 Flowable 任务，设置流程变量 `approvalResult=APPROVED`，流程流转至下一节点。若流程结束（最后一级审批通过），通过 BusinessTaskCallback 回调业务模块，更新 WorkflowInstance 状态为"已完成"

#### Scenario: 审批驳回
- **WHEN** 用户对任务调用完成接口，传入 `approved: false`
- **THEN** 系统完成 Flowable 任务，设置流程变量 `approvalResult=REJECTED`，流程流转至驳回结束节点，通过 BusinessTaskCallback 回调业务模块，更新 WorkflowInstance 状态为"已终止"

#### Scenario: 任务不存在
- **WHEN** 请求的 taskId 在 Flowable 中不存在
- **THEN** 系统返回 BusinessException，错误码 6001

#### Scenario: 任务不属于当前用户
- **WHEN** 当前用户不是该任务的 assignee
- **THEN** 系统返回 BusinessException，错误码 6002

### Requirement: 通用查询审批历史
系统 SHALL 在 `GET /api/tasks/history` 端点返回任意业务类型的审批历史。

#### Scenario: 查询审批历史
- **WHEN** 请求 `GET /api/tasks/history?businessType=PURCHASE_REQUEST&businessId=123`
- **THEN** 系统通过 businessType + businessId 查询 WorkflowInstance 获取 processInstanceId，再查询 Flowable 历史任务表，返回各审批节点的 nodeName、assignee、result、comment、time

#### Scenario: 无审批记录
- **WHEN** 指定 businessType + businessId 对应的业务尚未启动审批流程
- **THEN** 系统返回空列表

### Requirement: WorkflowInstance 实体与持久化
系统 SHALL 通过 `WorkflowInstance` 领域实体和 `t_workflow_instance` 表管理业务与流程实例的关联。

#### Scenario: 启动流程时创建记录
- **WHEN** 系统成功启动一个审批流程
- **THEN** 在 t_workflow_instance 表中插入一条记录，包含 businessType、businessId、processInstanceId，状态为"运行中"

#### Scenario: 流程结束时更新状态
- **WHEN** 审批流程结束（通过或驳回）
- **THEN** 更新对应 WorkflowInstance 记录的状态为"已完成"或"已终止"

#### Scenario: 根据业务信息查询
- **WHEN** 系统需要查询某个业务对象的流程实例
- **THEN** 可通过 businessType + businessId 唯一索引快速定位

### Requirement: WorkflowTask 领域实体
系统 SHALL 使用 `WorkflowTask` 领域实体包装 Flowable 原生 Task，domain 层不直接暴露 Flowable 类型。

#### Scenario: 从 Flowable Task 构建
- **WHEN** WorkflowTaskRepository 查询到 Flowable 任务
- **THEN** 通过静态工厂方法将 Flowable Task 转换为 WorkflowTask 实体，提取 taskId、nodeName、businessType、businessId、businessTitle、businessSubtitle、createTime
