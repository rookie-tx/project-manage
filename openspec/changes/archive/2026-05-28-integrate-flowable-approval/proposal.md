## Why

当前采购申请的状态流转（待采购→已取消）是一个硬编码的枚举状态机，无法支持审批流程。采购申请需要经过"提交审批→部门审批→财务审批"等环节后才能确认采购，这是企业采购管理的核心需求。

## What Changes

- **BREAKING**: PurchaseRequest 状态枚举从 PENDING/CANCELLED 扩展为 PENDING/APPROVING/APPROVED/REJECTED/CANCELLED，废弃 `canTransitionTo()` 方法
- PurchaseRequest 实体新增 `processInstanceId` 字段，关联 Flowable 流程实例
- 集成 Flowable 工作流引擎（`flowable-spring-boot-starter`），引入 BPMN 2.0 流程定义
- 新增"提交审批"操作：采购申请由"待采购"进入审批流程
- 新增"我的待办"接口：查询当前用户的审批任务列表
- 新增"审批通过/驳回"接口：完成审批节点，写入审批意见
- 前端新增待办任务页面、审批操作按钮、审批历史时间线
- 列表页和详情页展示当前审批节点信息

## Capabilities

### New Capabilities
- `workflow-engine-integration`: Flowable 引擎集成，包括依赖配置、BPMN 流程定义、流程实例管理
- `approval-task-management`: 审批任务管理，包括待办列表查询、审批通过/驳回操作、审批意见记录
- `approval-task-ui`: 前端审批页面，包括待办任务列表、审批操作按钮、审批历史时间线

### Modified Capabilities
- `purchase-request-api`: 采购申请增加提交审批端点，状态枚举扩展，实体关联流程实例 ID
- `purchase-request-ui`: 列表/详情页展示审批状态和节点信息，操作按钮增加"提交审批"和审批相关操作

## Impact

- **数据库**: `t_purchase_request` 表新增 `process_instance_id` 列；Flowable 自动创建 30+ 张 `ACT_*` 表
- **依赖**: `flowable-spring-boot-starter`（约 6.8+ 版本适配 Spring Boot 3.5）
- **domain 层**: PurchaseRequest 实体、PurchaseRequestStatus 枚举、PurchaseRequestDomainService
- **application 层**: PurchaseRequestApplicationService 新增审批相关方法
- **api 层**: 新增审批端点（提交审批、待办列表、审批操作）
- **前端**: 路由、页面、Store 均需扩展
