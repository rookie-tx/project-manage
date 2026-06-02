## 1. task 模块 domain 层（新增类型）

- [x] 1.1 创建 `BusinessType` 枚举，包含 `PURCHASE_REQUEST("purchase-request-approval")`，每个值携带 `processDefinitionKey`
- [x] 1.2 创建 `BusinessInfo` record，包含 `title` 和 `subtitle` 字段
- [x] 1.3 创建 `TaskResult` sealed interface，包含 `Approved` 和 `Rejected` 两种实现
- [x] 1.4 创建 `BusinessTaskCallback` 接口，定义 `getType()`、`getBusinessInfo(Long businessId)`、`onTaskCompleted(Long businessId, TaskResult result)` 三个方法
- [x] 1.5 创建 `WorkflowTask` 领域实体，私有构造 + `fromPersistence()` 静态工厂，包含 taskId、nodeName、businessType、businessId、businessTitle、businessSubtitle、createTime 字段
- [x] 1.6 创建 `WorkflowInstance` 领域实体，私有构造 + `create()` 和 `fromPersistence()` 静态工厂，包含 id、businessType、businessId、processInstanceId、status、createdAt、updatedAt
- [x] 1.7 创建 `WorkflowInstanceRepository` 仓储接口，定义 `save()`、`findByBusiness(businessType, businessId)`、`findByProcessInstanceId(processInstanceId)`、`updateStatus(id, status)` 方法
- [x] 1.8 重构 `WorkflowTaskRepository` 接口，`findById()` 返回 `Optional<WorkflowTask>`，`findByCandidateOrAssigned()` 返回 `List<WorkflowTask>`，新增 `findActiveByProcessInstanceId()`

## 2. task 模块 infrastructure 层（仓储实现 + 配置）

- [x] 2.1 创建 `WorkflowInstancePO` 持久化对象，映射 `t_workflow_instance` 表
- [x] 2.2 创建 `WorkflowInstanceMapper` MyBatis 接口（insert、selectByBusiness、selectByProcessInstanceId、updateStatus）
- [x] 2.3 创建 `WorkflowInstanceMapper.xml` MyBatis 映射文件
- [x] 2.4 创建 `WorkflowInstancePoMapper` MapStruct 接口，toDomain 使用 default 方法委托实体工厂，toPO 自动生成
- [x] 2.5 创建 `WorkflowInstanceRepositoryImpl` @Component 实现，注入 Mapper + PoMapper
- [x] 2.6 重构 `FlowableWorkflowTaskRepository`，适配返回 `WorkflowTask` 实体，从流程变量中提取 businessType、businessId、businessTitle、businessSubtitle
- [x] 2.7 创建 `CallbackRegistry` @Component，通过构造器注入 `List<BusinessTaskCallback>` 自动收集所有实现，维护 `Map<BusinessType, BusinessTaskCallback>` 并提供 `lookup(BusinessType)` 方法
- [x] 2.8 创建 `TaskDomainConfig` @Configuration，将 TaskApplicationService 暴露为 Bean

## 3. task 模块 application 层（服务重构）

- [x] 3.1 重构 `TaskApplicationService`：移除 `PurchaseRequestRepository` 依赖，注入 `CallbackRegistry`、`WorkflowInstanceRepository`、`RuntimeService`、`HistoryService`
- [x] 3.2 新增 `startProcess(BusinessType, Long businessId, Map<String, Object> variables)` 方法：查找流程定义 Key → 调用 callback.getBusinessInfo() 注入展示变量 → 启动流程实例 → 保存 WorkflowInstance
- [x] 3.3 重构 `listTasks()`：使用 WorkflowTask 实体，不再查询 PurchaseRequest 表
- [x] 3.4 重构 `completeTask()`：完成任务后通过 CallbackRegistry 查找回调，调用 `callback.onTaskCompleted()` 通知业务模块，更新 WorkflowInstance 状态
- [x] 3.5 新增 `getHistory(BusinessType, Long businessId)` 方法：通过 WorkflowInstanceRepository 查 processInstanceId → 查 Flowable 历史任务表 → 返回审批历史列表
- [x] 3.6 创建 `HistoryResponse` record（task.application 包），`WorkflowTask → TaskResponse` 映射在 ApplicationService 内联处理

## 4. task 模块 api 层（端点更新）

- [x] 4.1 重构 `TaskResponse`：将 `purchaseRequestId`/`prName`/`projectName` 替换为 `businessType`/`businessId`/`businessTitle`/`businessSubtitle`
- [x] 4.2 创建 `HistoryResponse` DTO（task.application 包），包含 `nodeName`、`assignee`、`result`、`comment`、`time`
- [x] 4.3 重构 `TaskController.list()`：适配新的 TaskResponse 字段
- [x] 4.4 新增 `GET /api/tasks/history` 端点，参数 `businessType` + `businessId`
- [x] 4.5 更新 `ErrorCode` 枚举，新增通用任务错误码（6003-6005），移除 PR 专属的 4004/4005

## 5. 数据库变更

- [x] 5.1 创建 `t_workflow_instance` 表 DDL（包含 unique 索引 on business_type + business_id）
- [x] 5.2 从 `t_purchase_request` 移除 `process_instance_id` 列

## 6. purchaserequest 模块重构

- [x] 6.1 重构 `PurchaseRequest` 领域实体：移除 `processInstanceId` 字段、`setProcessInstanceId()` 方法、`fromPersistence()` 中对应参数
- [x] 6.2 重构 `PurchaseRequestPO`：移除 `processInstanceId` 字段
- [x] 6.3 重构 `PurchaseRequestPoMapper`：toDomain 和 toPO 适配移除的字段
- [x] 6.4 更新 `PurchaseRequestMapper.xml`：移除 `process_instance_id` 列映射
- [x] 6.5 创建 `PRTaskCallback` @Component，实现 `BusinessTaskCallback`：getType() 返回 PURCHASE_REQUEST，getBusinessInfo() 查询 PR 名称和项目名称，onTaskCompleted() 更新 PR 状态
- [x] 6.6 重构 `PurchaseRequestApplicationService.submit()`：改为调用 `TaskApplicationService.startProcess()`，移除对 Flowable RuntimeService 的直接调用
- [x] 6.7 删除 `PurchaseRequestApplicationService.getApprovalHistory()`：Controller 直接委托给 taskApplicationService.getHistory()
- [x] 6.8 重构 `PurchaseRequestApplicationService.toResponse()`：当前审批节点信息改为调用 taskApplicationService.getCurrentApprovalNode()，移除对 Flowable TaskService 的直接调用
- [x] 6.9 移除 `PurchaseRequestApplicationService` 中不再需要的 Flowable 依赖注入（RuntimeService、TaskService、HistoryService）
- [x] 6.10 更新 `ErrorCode`：移除 4004 PR_TASK_NOT_OWNER，移除 4005 PR_APPROVAL_START_FAILED

## 7. BPMN 流程定义更新

- [x] 7.1 BPMN 文件不直接引用 `purchaseRequestId`，无需修改；现在 `businessId` 在 `startProcess()` 中自动注入流程变量

## 8. 前端适配

- [x] 8.1 重构 `approval` store：tasks 适配新字段 `businessType`、`businessId`、`businessTitle`、`businessSubtitle`；新增 `fetchHistory()` 方法
- [x] 8.2 重构 `TaskListPage.vue`：表格列从 `prName`/`projectName` 改为 `businessTitle`/`businessSubtitle`
- [x] 8.3 重构 `ApprovalDialog.vue`：展示通用字段 `businessType`/`businessTitle`/`businessSubtitle`
- [x] 8.4 `purchaseRequest` store：`fetchApprovalHistory()` 保持不变（PR Controller 端点内部已委托给 task 模块）
- [x] 8.5 更新 `ApprovalHistory.vue`：字段 `approverName` → `assignee`（适配 task 模块 HistoryResponse）

## 9. 测试

- [ ] 9.1 编写 `TaskApplicationService` 单元测试（mock 所有依赖，验证 startProcess/listTasks/completeTask/getHistory 行为）
- [ ] 9.2 编写 `WorkflowInstanceRepositoryImpl` 集成测试（验证 CRUD 操作）
- [ ] 9.3 编写 `TaskController` Web MVC 切片测试（验证 GET /api/tasks、POST /api/tasks/{id}/complete、GET /api/tasks/history）
- [ ] 9.4 更新 `PurchaseRequestApplicationService` 单元测试（适配去除 Flowable 直接调用后的行为）
- [ ] 9.5 更新 `PurchaseRequestController` Web MVC 切片测试
