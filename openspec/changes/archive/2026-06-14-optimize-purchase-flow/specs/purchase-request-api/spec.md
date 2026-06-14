## MODIFIED Requirements

### Requirement: 提交审批
系统 SHALL 在 `POST /api/purchase-requests/{id}/submit` 端点提交采购申请进入审批，审批流程仅包含项目经理单节点。

#### Scenario: 成功提交
- **WHEN** 采购申请状态为"待提交"，用户提交审批人变量 `{ pmUserId }`
- **THEN** 系统调用 `TaskApplicationService.startProcess(BusinessType.PURCHASE_REQUEST, id, variables)`，状态变更为"审批中"，返回 HTTP 200

#### Scenario: 不可重复提交
- **WHEN** 采购申请状态不是"待提交"
- **THEN** 系统返回 BusinessException，错误码 4003

#### Scenario: 审批通过后自动创建 PO
- **WHEN** 项目经理审批通过该采购申请
- **THEN** 系统自动创建一条 PO 记录（状态=采购中），PO 关联该 PR 的一对一 purchaseRequestId，PR 中的名称/规格/品牌/数量/单位/到货日期/项目带入 PO
