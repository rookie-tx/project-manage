# purchase-order-api Delta Specification

## ADDED Requirements

### Requirement: 采购单报价列表

系统 SHALL 在 `GET /api/purchase-orders/{id}/quotations` 端点返回该采购单收到的所有报价记录。

#### Scenario: 成功获取报价列表

- **WHEN** 管理员请求某采购单的报价列表
- **THEN** 系统返回该采购单下所有未删除的报价记录，每条包含报价 ID、供应商 ID、供应商公司名、联系人、联系电话、单价、总金额、交货日期、备注、报价状态（code+label）、创建时间、更新时间

#### Scenario: 采购单不存在

- **WHEN** 请求的采购单 ID 不存在或已软删除
- **THEN** 系统返回错误码 PO_NOT_FOUND

### Requirement: 选用报价

系统 SHALL 在 `POST /api/purchase-orders/{orderId}/quotations/{quotationId}/select` 端点支持管理员选用报价。

#### Scenario: 成功选用

- **WHEN** 管理员选用 PENDING 状态且关联采购单为 PURCHASING 状态的报价
- **THEN** 系统原子性地将报价状态更新为 SELECTED、将供应商信息写入采购单、采购单状态流转为 PURCHASED、记录 purchaserId，返回更新后的采购单信息

#### Scenario: 报价不属于该采购单

- **WHEN** 请求的 quotationId 对应的报价的 purchaseOrderId 与路径 orderId 不匹配
- **THEN** 系统返回错误「报价不属于该采购单」

#### Scenario: 报价已被选用

- **WHEN** 请求的报价状态已为 SELECTED
- **THEN** 系统返回错误码 QUOTATION_ALREADY_SELECTED

#### Scenario: 采购单状态不允许选用

- **WHEN** 关联的采购单状态不是 PURCHASING
- **THEN** 系统返回错误码 PO_STATUS_INVALID
