## MODIFIED Requirements

### Requirement: 采购申请审批流程定义为单节点
系统 SHALL 将采购申请审批的 BPMN 流程定义为仅包含项目经理审批的单节点流程。

#### Scenario: 项目经理审批通过
- **WHEN** 项目经理对采购申请任务调用完成接口，传入 `approved: true`
- **THEN** 系统完成 Flowable 任务，设置 `approvalResult=APPROVED`，流程到达 approved 结束节点，通过 BusinessTaskCallback.onTaskCompleted() 回调业务模块创建 PO

#### Scenario: 项目经理审批驳回
- **WHEN** 项目经理对采购申请任务调用完成接口，传入 `approved: false`
- **THEN** 系统完成 Flowable 任务，设置 `approvalResult=REJECTED`，流程到达 rejected 结束节点，PR 状态变更为"已驳回"

### Requirement: 提交审批仅需项目经理变量
系统 SHALL 在启动采购申请审批流程时仅要求传入 `pmUserId` 变量。

#### Scenario: 启动流程
- **WHEN** 用户提交采购申请审批，传入 `{ pmUserId: 123 }`
- **THEN** 系统启动 BPMN 流程，将 pmUserId 设为审批节点 assignee

#### Scenario: 缺少审批人
- **WHEN** 启动流程时未传入 pmUserId 或 pmUserId 对应的用户不存在
- **THEN** 系统返回校验错误
