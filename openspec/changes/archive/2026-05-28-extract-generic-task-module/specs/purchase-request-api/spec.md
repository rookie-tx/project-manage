## MODIFIED Requirements

### Requirement: 提交审批
系统 SHALL 在 `POST /api/purchase-requests/{id}/submit` 端点提交采购申请进入审批，内部委托 task 模块处理。

#### Scenario: 成功提交
- **WHEN** 采购申请状态为"待提交"，用户提交审批人信息
- **THEN** 系统调用 `TaskApplicationService.startProcess(BusinessType.PURCHASE_REQUEST, id, variables)`，状态变更为"审批中"，返回 HTTP 200

#### Scenario: 不可重复提交
- **WHEN** 采购申请状态不是"待提交"
- **THEN** 系统返回 BusinessException，错误码 4003

## REMOVED Requirements

### Requirement: 查询审批历史
**Reason**: 审批历史查询移至 task 模块通用端点 `GET /api/tasks/history?businessType=PURCHASE_REQUEST&businessId={id}`
**Migration**: 前端详情页改为调用 task 模块通用审批历史接口，而非 `/api/purchase-requests/{id}/approval-history`
