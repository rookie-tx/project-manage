## Requirements
### Requirement: 创建采购申请

系统 SHALL 在 `POST /api/purchase-requests` 端点创建新的采购申请，物料信息通过 `materialId` 从物料库关联。

#### Scenario: 成功创建

- **WHEN** 用户提交完整的采购申请数据（projectId、materialId、brand、quantity、arrivalDate）
- **THEN** 系统根据 materialId 查询物料表获取 name、spec、unit（字典 code），自动填入采购申请，状态初始为"待提交"，返回 HTTP 200 及完整数据（含物料信息）

#### Scenario: 物料不存在

- **WHEN** 用户提交的 materialId 在 t_material 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4004

#### Scenario: 项目不存在

- **WHEN** 用户提交的 projectId 在 t_project 中不存在或已删除
- **THEN** 系统返回 BusinessException，错误码 4001

#### Scenario: 必填校验失败

- **WHEN** 用户未填写 materialId 字段
- **THEN** 系统返回参数校验错误，错误码 1002

### Requirement: 查询采购申请列表
系统 SHALL 在 `GET /api/purchase-requests` 端点返回分页列表，支持按项目筛选。

#### Scenario: 无筛选查询
- **WHEN** 请求不带任何筛选参数，page=1, size=20
- **THEN** 系统返回第 1 页数据（每页 20 条），包含 total/page/size 及 items 数组，按创建时间倒序

#### Scenario: 按项目筛选
- **WHEN** 请求带 projectId 参数
- **THEN** 系统只返回该项目的采购申请

#### Scenario: 自动排除已删除记录
- **WHEN** 查询列表
- **THEN** 不返回 isDeleted=1 的记录

### Requirement: 查询采购申请详情
系统 SHALL 在 `GET /api/purchase-requests/{id}` 端点返回采购申请详情。

#### Scenario: 成功查询
- **WHEN** 请求存在的采购申请 ID
- **THEN** 系统返回完整字段（含项目名称通过 projectId 联查）

#### Scenario: 不存在
- **WHEN** 请求的 ID 不存在或已被软删除
- **THEN** 系统返回 BusinessException，错误码 4002

### Requirement: 更新采购申请

系统 SHALL 在 `PUT /api/purchase-requests/{id}` 端点支持部分更新。

#### Scenario: 更新基本信息

- **WHEN** 采购申请状态为"待提交"，用户修改品牌、数量等字段并提交
- **THEN** 系统更新对应字段，若 materialId 变更则重新从物料库获取 name/spec/unit，返回 HTTP 200 及更新后数据

#### Scenario: 非待提交状态不可编辑

- **WHEN** 采购申请状态为"审批中"或其他非待提交状态，用户尝试编辑
- **THEN** 系统返回 BusinessException，错误码 4003

### Requirement: 采购申请响应包含物料信息

系统 SHALL 在采购申请列表和详情响应中返回关联的物料信息，单位相关字段使用字典 code（String 类型）。

#### Scenario: 列表响应

- **WHEN** 查询采购申请列表
- **THEN** 每条记录包含 `materialId`、`materialName`、`materialSpec`、`materialUnit` 字段（均为 String 或 Long 类型），`unitCode` 字段为 String 类型的字典 code（如 "TON"），不再提供 `unitLabel` 字段（前端根据 `unitCode` 自行查字典获取中文标签）

#### Scenario: 详情响应

- **WHEN** 查询采购申请详情
- **THEN** 响应包含 `materialId`、`materialName`、`materialSpec`、`materialUnit` 字段，`unitCode` 为 String 类型的字典 code

### Requirement: 物料不存在错误码

采购申请模块 SHALL 新增物料不存在错误码。

| 错误码 | 含义 |
|--------|------|
| 4004 | 物料不存在 |

### Requirement: 软删除采购申请
系统 SHALL 在 `DELETE /api/purchase-requests/{id}` 端点支持软删除。

#### Scenario: 成功删除
- **WHEN** 请求存在的采购申请 ID
- **THEN** 系统将 isDeleted 标记为 1，返回 HTTP 200，data 为 null

#### Scenario: 删除不存在的记录
- **WHEN** 请求不存在或已删除的 ID
- **THEN** 系统返回 BusinessException，错误码 4002

### Requirement: ErrorCode 范围
采购申请相关错误 SHALL 使用 4xxx 范围。

| 错误码 | 含义 |
|--------|------|
| 4001 | 项目不存在 |
| 4002 | 采购申请不存在 |
| 4003 | 状态操作不合法 |
| 4004 | 物料不存在 |

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

