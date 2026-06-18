# quotation-management Specification

## Purpose

报价管理核心后端，定义报价实体的数据模型、生命周期、API 接口和业务规则。

## ADDED Requirements

### Requirement: 报价数据模型

系统 SHALL 在 `t_quotation` 表中存储供应商对采购单的报价记录。

字段说明：
- `id`: 主键
- `purchase_order_id`: 关联采购单 ID，FK → t_purchase_order
- `supplier_id`: 关联供应商 ID，FK → t_supplier
- `unit_price`: 单价
- `total_amount`: 总金额
- `delivery_date`: 预计交货日期
- `remark`: 备注
- `status`: 报价状态（0=待审核, 1=已选用）
- `created_by`: 创建人 ID（供应商用户）

约束：
- `(purchase_order_id, supplier_id)` 组合唯一，同一供应商对同一采购单只能有一条报价
- 软删除机制（is_deleted）

#### Scenario: 报价唯一性约束

- **WHEN** 供应商 A 已对采购单 X 提交报价，再次尝试创建新报价
- **THEN** 系统更新供应商 A 的已有报价记录，而非创建第二条

### Requirement: 供应商提交报价

系统 SHALL 允许供应商对 PURCHASING 状态的采购单提交报价。

#### Scenario: 首次提交报价

- **WHEN** 供应商对状态为 PURCHASING 的采购单提交 unitPrice、totalAmount、deliveryDate、remark
- **THEN** 系统创建 Quotation 记录，状态为 PENDING，createdBy 设为当前供应商用户 ID

#### Scenario: 修改已有报价

- **WHEN** 供应商对已报价的采购单再次提交报价
- **THEN** 系统更新已有报价记录的 unitPrice、totalAmount、deliveryDate、remark，状态重置为 PENDING

#### Scenario: 非采购中状态不可报价

- **WHEN** 供应商对状态为 PURCHASED 或 ARRIVED 的采购单提交报价
- **THEN** 系统返回错误「该采购单当前状态不可报价」

#### Scenario: 非供应商不可报价

- **WHEN** 非供应商角色的用户尝试提交报价
- **THEN** 系统返回权限拒绝错误

### Requirement: 供应商查看自己的报价

系统 SHALL 允许供应商查看自己提交的报价列表和详情。

#### Scenario: 供应商报价列表

- **WHEN** 供应商请求 `GET /api/supplier/quotations`
- **THEN** 系统返回当前供应商用户 submitted 的所有报价记录，分页展示，包含关联的采购单信息和物料信息

#### Scenario: 供应商报价详情

- **WHEN** 供应商请求某条报价的详情
- **THEN** 系统返回报价完整字段及关联的采购单信息（物料名称、规格、数量、单位等）

#### Scenario: 不能查看其他供应商的报价

- **WHEN** 供应商 A 尝试查看供应商 B 的报价详情
- **THEN** 系统返回错误「无权查看该报价」

### Requirement: 管理员查看采购单的报价列表

系统 SHALL 在管理端提供查看某采购单所有报价的接口。

#### Scenario: 查看所有报价

- **WHEN** 管理员请求 `GET /api/purchase-orders/{orderId}/quotations`
- **THEN** 系统返回该采购单下所有未删除的报价记录，包含供应商名称、公司名、联系方式、报价金额、交货日期、报价时间、报价状态

#### Scenario: 采购单无报价

- **WHEN** 某采购单还没有供应商报价
- **THEN** 系统返回空列表

### Requirement: 管理员选用报价

系统 SHALL 支持管理员选用某条待审核状态的报价，自动更新采购单信息并完成采购。

#### Scenario: 成功选用报价

- **WHEN** 管理员对 PENDING 状态的报价执行选用操作
- **THEN** 系统在一个事务中执行：将报价状态更新为 SELECTED；将报价中的 supplierId 对应的 companyName、contactPhone 写入采购单的 supplierName、supplierPhone；将报价中的 unitPrice、totalAmount 写入采购单；采购单状态流转为 PURCHASED；记录 purchaserId 为当前操作用户

#### Scenario: 报价已被选用

- **WHEN** 管理员尝试选用状态已为 SELECTED 的报价
- **THEN** 系统返回错误「该报价已被选用」

#### Scenario: 采购单状态不匹配

- **WHEN** 管理员尝试选用报价，但关联的采购单状态不是 PURCHASING
- **THEN** 系统返回错误「采购单当前状态不可选用报价」

### Requirement: 供应商查看待报价采购单

系统 SHALL 允许供应商查看所有 PURCHASING 状态的采购单（即待报价的物料）。

#### Scenario: 待报价采购单列表

- **WHEN** 供应商请求 `GET /api/supplier/pending-orders`
- **THEN** 系统返回所有状态为 PURCHASING 的采购单列表，包含来源采购申请的物料名称、规格、品牌、数量、单位、项目名称、预计到货日期

#### Scenario: 待报价采购单详情

- **WHEN** 供应商请求某待报价采购单的详情
- **THEN** 系统返回该采购单的完整信息，包含物料详情和项目信息，以及当前供应商是否已提交报价及报价内容

### Requirement: 报价软删除

系统 SHALL 允许供应商软删除自己提交的 PENDING 状态报价。

#### Scenario: 成功删除

- **WHEN** 供应商删除自己提交的 PENDING 状态报价
- **THEN** 系统标记 is_deleted=1

#### Scenario: 不可删除已选用报价

- **WHEN** 供应商尝试删除状态为 SELECTED 的报价
- **THEN** 系统返回错误「已选用的报价不可删除」
