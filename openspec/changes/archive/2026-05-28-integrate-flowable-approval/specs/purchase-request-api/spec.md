## MODIFIED Requirements

### Requirement: 创建采购申请
系统 SHALL 在 `POST /api/purchase-requests` 端点创建新的采购申请。

#### Scenario: 成功创建
- **WHEN** 用户提交完整的采购申请数据（projectId、name、specification、brand、quantity、unit、arrivalDate）
- **THEN** 系统创建采购申请，状态初始为"待提交"，返回 HTTP 200 及完整数据

#### Scenario: 项目不存在
- **WHEN** 用户提交的 projectId 在 t_project 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4001

#### Scenario: 必填校验失败
- **WHEN** 用户未填写 name 字段
- **THEN** 系统返回参数校验错误，错误码 1002

### Requirement: 更新采购申请
系统 SHALL 在 `PUT /api/purchase-requests/{id}` 端点支持部分更新。

#### Scenario: 更新基本信息
- **WHEN** 采购申请状态为"待提交"，用户修改名称、规格、品牌等字段并提交
- **THEN** 系统更新对应字段，返回 HTTP 200 及更新后数据

#### Scenario: 非待提交状态不可编辑
- **WHEN** 采购申请状态为"审批中"或其他非待提交状态，用户尝试编辑
- **THEN** 系统返回 BusinessException，错误码 4003

### Requirement: ErrorCode 范围
采购申请相关错误 SHALL 使用 4xxx 范围。

| 错误码 | 含义 |
|--------|------|
| 4001 | 项目不存在 |
| 4002 | 采购申请不存在 |
| 4003 | 状态操作不合法 |

## ADDED Requirements

### Requirement: 提交审批
系统 SHALL 在 `POST /api/tasks/start` 端点提交采购申请进入审批。

#### Scenario: 成功提交
- **WHEN** 采购申请状态为"待提交"，用户通过任务模块提交审批（businessType=PURCHASE_REQUEST, businessId）
- **THEN** 系统启动 Flowable 流程实例，状态变更为"审批中"，返回 HTTP 200

#### Scenario: 不可重复提交
- **WHEN** 采购申请已存在进行中的审批流程
- **THEN** 系统返回 BusinessException，错误码 6004

### Requirement: 查询审批历史
系统 SHALL 在 `GET /api/tasks/history` 端点返回审批记录。

#### Scenario: 查询审批历史
- **WHEN** 采购申请存在审批记录
- **THEN** 系统返回各审批节点的审批人、时间、结果、意见
