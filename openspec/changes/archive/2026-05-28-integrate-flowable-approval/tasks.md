## 1. 后端：Flowable 引擎集成

- [x] 1.1 pom.xml 新增 `flowable-spring-boot-starter` 依赖（7.0.0）
- [x] 1.2 application.yml 配置 Flowable（database-schema-update: true, async-executor-activate: false）
- [x] 1.3 创建 `src/main/resources/processes/purchase-request-approval.bpmn20.xml` BPMN 流程定义（三级顺序审批：部门经理→财务→总经理，驳回直接结束）

## 2. 后端：domain 层改造

- [x] 2.1 PurchaseRequestStatus 枚举重构：PENDING(0,"待提交")/APPROVING(1,"审批中")/APPROVED(2,"已通过")/REJECTED(3,"已驳回")/CANCELLED(4,"已取消")，移除 canTransitionTo()
- [x] 2.2 PurchaseRequest 实体新增 processInstanceId(String) 字段（create/factory/PO/XML 同步更新）
- [x] 2.3 PurchaseRequestDomainService.update 移除状态流转逻辑，仅处理字段更新
- [x] 2.4 PurchaseRequestRepository 新增 updateStatus 方法（按流程状态同步更新 status）

## 3. 后端：infrastructure 层

- [x] 3.1 UserMapper/UserMapper.xml 新增 selectById 查询（用于验证审批人存在）
- [x] 3.2 UserPoMapper 新增 toDomain 方法（fromPersistence 工厂兼容）
- [x] 3.3 PurchaseRequestPO 新增 processInstanceId 字段
- [x] 3.4 PurchaseRequestMapper.xml 新增 updateStatus SQL、insert/selectByPage/selectById 增加 process_instance_id
- [x] 3.5 PurchaseRequestPoMapper 适配新字段
- [x] 3.6 PurchaseRequestRepositoryImpl 实现新增方法
- [x] 3.7 schema.sql t_purchase_request 新增 `process_instance_id VARCHAR(64)` 列

## 4. 后端：application 层

- [x] 4.1 PurchaseRequestApplicationService 新增 submit(id, approverIds) 方法（校验状态→启动流程→更新状态）
- [x] 4.2 PurchaseRequestApplicationService.toResponse 填充当前审批节点信息（通过 processInstanceId 查询 act_ru_task）
- [x] 4.3 新建 ApprovalHistoryResponse DTO（nodeName, approverName, result, comment, time）
- [x] 4.4 PurchaseRequestApplicationService 新增 getApprovalHistory(id) 方法

## 5. 后端：api 层

- [x] 5.1 新建 SubmitApprovalRequest DTO（deptManagerId/financeManagerId/generalManagerId）
- [x] 5.2 新建 TaskResponse DTO（taskId, purchaseRequestId, prName, projectName, nodeName, createTime）
- [x] 5.3 新建 CompleteTaskRequest DTO（approved: Boolean, comment: String）
- [x] 5.4 PurchaseRequestController 新增 POST /{id}/submit、GET /{id}/approval-history 端点
- [x] 5.5 新建 TaskController（GET /api/tasks 待办列表、POST /api/tasks/{taskId}/complete 完成任务）
- [x] 5.6 ErrorCode 枚举新增 4004 任务不属于当前用户、4005 审批流程启动失败

## 6. 前端：Store

- [x] 6.1 新建 stores/approval.js — useApprovalStore（fetchTasks/completeTask 方法，待办任务列表）
- [x] 6.2 stores/purchaseRequest.js 新增 submitApproval(id, approverIds)、fetchApprovalHistory(id) 方法
- [x] 6.3 stores/purchaseRequest.js 状态映射更新为 5 种状态（待提交/审批中/已通过/已驳回/已取消）

## 7. 前端：路由

- [x] 7.1 router/index.js 新增 /tasks 路由（待办任务页面）
- [x] 7.2 DashboardLayout.vue 顶栏新增"待办任务"菜单项（带徽章显示数量）

## 8. 前端：待办任务页面

- [x] 8.1 新建 views/approval/TaskListPage.vue — a-table 展示待办列表（采购申请名称、项目名称、当前节点、到达时间），行点击触发审批对话框
- [x] 8.2 新建 components/ApprovalDialog.vue — 模态框展示采购申请摘要、审批意见输入、通过/驳回按钮

## 9. 前端：审批组件

- [x] 9.1 新建 components/ApprovalHistory.vue — 纵向时间线组件（a-timeline），展示审批节点、审批人、时间、结果、意见
- [x] 9.2 新建 components/SubmitApprovalDialog.vue — 提交审批对话框，三个审批人下拉选择（调 /api/users），确认后调 submitApproval

## 10. 前端：采购申请页面改造

- [x] 10.1 PurchaseRequestListPage.vue 操作列按状态展示不同按钮（待提交→提交审批+编辑+删除；审批中→查看；已通过/已驳回→查看），状态列显示中文
- [x] 10.2 PurchaseRequestDetailPage.vue 底部集成 ApprovalHistory 组件，操作按钮按状态适配，待提交状态显示"提交审批"
- [x] 10.3 PurchaseRequestEditPage.vue 非待提交状态进入时提示并重定向回详情页
