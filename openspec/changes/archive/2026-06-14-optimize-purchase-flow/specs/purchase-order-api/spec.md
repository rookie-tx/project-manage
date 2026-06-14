## MODIFIED Requirements

### Requirement: 创建采购记录
系统 SHALL 在 `POST /api/purchase-orders` 端点创建采购记录，PR 审批通过后自动创建，用户仅需传入 purchaseRequestId。

#### Scenario: 成功创建（自动）
- **WHEN** PR 审批通过后系统自动调用，传入 purchaseRequestId
- **THEN** 系统创建 PO（状态=PURCHASING），关联 PR 的 1:1 purchaseRequestId，名称/规格/品牌/数量/单位/到货日期/项目从 PR 带入，供应商和价格留空

#### Scenario: 采购申请已存在 PO
- **WHEN** 传入的 purchaseRequestId 已有关联的 PO（UNIQUE 约束）
- **THEN** 系统返回错误码 PO_DUPLICATE

### Requirement: 更新采购记录（补充供应商信息）
系统 SHALL 在 `PUT /api/purchase-orders/{id}` 端点支持补充编辑供应商信息和价格。

#### Scenario: 补充供应商信息
- **WHEN** PO 状态为"采购中"（PURCHASING），用户提交 supplierName、supplierPhone、unitPrice、totalAmount、purchaseDate
- **THEN** 系统更新 PO 字段，PO 从"待采购"变为"采购中"（数据层面用 supplierName 是否非空区分）

#### Scenario: 更新文件
- **WHEN** 用户同时提交文件 ID
- **THEN** 系统将文件关联到该 PO

### Requirement: 采购记录列表
系统 SHALL 在 `GET /api/purchase-orders` 端点返回分页列表，支持状态、Tab 筛选。

#### Scenario: 待采购 Tab
- **WHEN** 请求 `GET /api/purchase-orders?tab=pending`
- **THEN** 系统返回状态为 PURCHASING 且 supplierName 为空的 PO 列表

#### Scenario: 采购中 Tab
- **WHEN** 请求 `GET /api/purchase-orders?tab=active`
- **THEN** 系统返回状态为 PURCHASING 且 supplierName 不为空的 PO 列表

#### Scenario: 已采购 Tab
- **WHEN** 请求 `GET /api/purchase-orders?status=1`
- **THEN** 系统返回状态为"已采购"的 PO 列表

#### Scenario: 已到货 Tab
- **WHEN** 请求 `GET /api/purchase-orders?status=2`
- **THEN** 系统返回状态为"已到货"的 PO 列表

### Requirement: 状态流转
系统 SHALL 在 `PUT /api/purchase-orders/{id}/status` 端点支持状态变更。

#### Scenario: 采购中→已采购
- **WHEN** PO 状态为"采购中"（PURCHASING），且 supplierName 非空，传入 status=1
- **THEN** 系统校验供应商信息已填写，更新状态为"已采购"

#### Scenario: 已采购→已到货
- **WHEN** PO 状态为"已采购"（PURCHASED），入库操作后传入 status=2
- **THEN** 系统更新状态为"已到货"

#### Scenario: 待采购不能直接完成
- **WHEN** PO 状态为"采购中"但 supplierName 为空，尝试变更到"已采购"
- **THEN** 系统返回错误"请先完善供应商信息"

### Requirement: PR 一对一关联
系统 SHALL 在 `t_purchase_order` 表中通过 `purchase_request_id` 字段建立与 PR 的一对一关联。

#### Scenario: 通过 PR ID 查询 PO
- **WHEN** 查询某采购申请关联的采购记录
- **THEN** 系统通过 purchase_request_id 唯一确定一条 PO 记录

#### Scenario: 重复创建被拒绝
- **WHEN** 尝试为已有 PO 的 PR 再次创建 PO
- **THEN** 系统返回 UNIQUE 约束冲突错误

## REMOVED Requirements

### Requirement: 创建采购记录（旧）
**Reason**: PR 审批通过后自动创建 PO，不再支持手动关联多个 PR
**Migration**: 调用方改用 `POST /api/purchase-orders` 仅传 purchaseRequestId，系统自动从 PR 复制基本信息。历史多对多数据保留在 t_purchase_order_item 表中仅用于查询
