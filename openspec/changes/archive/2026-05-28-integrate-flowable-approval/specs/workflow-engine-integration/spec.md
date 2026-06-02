## ADDED Requirements

### Requirement: Flowable 引擎依赖集成
系统 SHALL 引入 `flowable-spring-boot-starter` 依赖，启用 Flowable 流程引擎。

#### Scenario: 应用启动时自动配置
- **WHEN** 应用启动
- **THEN** Flowable 自动配置 DataSource、ProcessEngine，在 PostgreSQL 中创建 `ACT_*` 系列表

#### Scenario: BPMN 流程定义自动部署
- **WHEN** 应用启动且 `src/main/resources/processes/` 目录下有 `.bpmn20.xml` 文件
- **THEN** Flowable 自动解析并部署流程定义到引擎

### Requirement: 采购审批流程定义
系统 SHALL 提供一个名为 `purchase-request-approval` 的 BPMN 2.0 流程定义，描述三级审批流程。

#### Scenario: 流程启动
- **WHEN** 用户提交采购申请进入审批
- **THEN** 流程实例启动，进入第一个 UserTask——部门经理审批

#### Scenario: 三级顺序审批
- **WHEN** 部门经理审批通过
- **THEN** 流程流转至财务审批节点
- **WHEN** 财务审批通过
- **THEN** 流程流转至总经理审批节点
- **WHEN** 总经理审批通过
- **THEN** 流程结束，采购申请状态变更为"已通过"

#### Scenario: 审批驳回
- **WHEN** 任意审批节点执行驳回
- **THEN** 流程立即结束，采购申请状态变更为"已驳回"

### Requirement: 流程变量传递
系统 SHALL 在启动流程实例时传入审批人 ID 和采购申请 ID 作为流程变量。

#### Scenario: 启动时传参
- **WHEN** 启动 `purchase-request-approval` 流程
- **THEN** 传入 `purchaseRequestId`、`deptManagerId`、`financeManagerId`、`generalManagerId` 变量，UserTask 的 assignee 表达式引用对应变量
