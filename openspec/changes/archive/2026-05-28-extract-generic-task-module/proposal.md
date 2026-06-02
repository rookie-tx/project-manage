## Why

当前 task 模块与 `purchaserequest` 业务强耦合——`TaskApplicationService` 直接依赖 `PurchaseRequestRepository`，`TaskResponse` 包含 PR 专属字段，`PurchaseRequestApplicationService` 散落着对 Flowable API 的直接调用。每新增一种需要审批的业务（如项目立项审批），这些耦合点都要重复改造。需要将 task 模块抽象为独立的通用审批引擎，业务模块通过 `businessId + businessType` 与之交互。

## What Changes

- **新增** `WorkflowInstance` 领域实体与 `t_workflow_instance` 表，统一管理业务与流程实例的关联，替代业务表中散落的 `process_instance_id` 字段
- **新增** `BusinessType` 枚举，每个枚举值自带 `processDefinitionKey` 映射
- **新增** `BusinessTaskCallback` 接口，业务模块实现此接口以注册回调（获取展示信息、任务完成后处理）
- **新增** `WorkflowTask` 领域实体，包装 Flowable 原生 `Task`，使 domain 层保持零框架注解
- **重构** `TaskApplicationService`：移除对 `PurchaseRequestRepository` 的依赖，改为通过 `BusinessTaskCallback` 回调处理；新增 `startProcess()`、`getHistory()` 通用方法
- **重构** `TaskResponse`：将 `purchaseRequestId`/`prName`/`projectName` 替换为通用字段 `businessType`/`businessId`/`businessTitle`/`businessSubtitle`
- **新增** `GET /api/tasks/history?businessType=&businessId=` 通用审批历史端点
- **重构** `PurchaseRequestApplicationService`：`submit()` 改为调用 `TaskApplicationService.startProcess()`，移除对 Flowable API 的直接调用；删除 `getApprovalHistory()`（改用 task 通用接口）
- **BREAKING** `PurchaseRequest` 实体移除 `processInstanceId` 字段，数据库 `t_purchase_request` 移除对应列
- **重构** 前端 `TaskListPage`、`ApprovalDialog`、`approval` store 适配通用字段

## Capabilities

### New Capabilities
- `generic-task-module`: 独立于业务的通用任务审批引擎，通过 `businessType + businessId` 操作任务，支持启动流程、查询待办、完成任务、查询审批历史
- `business-task-callback`: 业务模块通过实现 `BusinessTaskCallback` 接口注册到 task 模块，提供业务展示信息和任务完成后的回调处理

### Modified Capabilities
- `approval-task-management`: 待办任务列表查询和任务完成改为通用实现，不再耦合采购申请；审批历史从 PR 专属端点改为 task 通用端点
- `approval-task-ui`: 待办列表页和审批对话框适配通用字段，根据 `businessType` 动态加载业务摘要
- `purchase-request-api`: 提交审批改为调用 task 模块 `startProcess`，移除对 Flowable API 的直接调用；审批历史改用 task 通用端点

## Impact

- 后端 `task` 模块：domain 层新增 5 个类型，infrastructure 层新增 5 个文件，application 层重构 Service，api 层新增 1 个端点 + 重构 Response
- 后端 `purchaserequest` 模块：domain 实体去 `processInstanceId`，application 去 Flowable 直接调用，infrastructure 新增 Callback 实现
- 数据库：新增 `t_workflow_instance` 表；`t_purchase_request` 移除 `process_instance_id` 列
- 前端：`TaskListPage`、`ApprovalDialog`、`approval` store 字段适配
