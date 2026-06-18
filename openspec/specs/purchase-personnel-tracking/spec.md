# purchase-personnel-tracking Specification

## Purpose

采购单人员追踪 — 在采购单上独立记录采购人和入库人，采购完成和入库操作时自动赋值，列表和详情展示用户名。

## Requirements

### Requirement: 采购人记录

系统 SHALL 在采购单状态从「采购中」变更为「已采购」时，自动将当前登录用户记录为该采购单的采购人。

#### Scenario: 采购完成自动记录采购人

- **WHEN** 用户对状态为「采购中」的采购单执行采购完成操作（`PUT /api/purchase-orders/{id}/status`，传入 status=1）
- **THEN** 系统校验供应商信息已填写后，将 `purchaser_id` 设置为 `LoginContextHolder.getUserId()`，状态变更为「已采购」

#### Scenario: 采购人姓名解析

- **WHEN** 查询采购单列表或详情
- **THEN** 系统通过 `UserRepository.findById(purchaserId)` 解析用户名，优先取 `user.getName()`，降级取 `user.getUsername()`，在响应中返回 `purchaserName` 字段

#### Scenario: 采购人不存在

- **WHEN** `purchaser_id` 对应的用户已被删除或不存在
- **THEN** 系统返回 `purchaserId` 原值，`purchaserName` 为 null

### Requirement: 入库人记录

系统 SHALL 在入库操作将采购单状态从「已采购」变更为「已到货」时，自动将当前登录用户记录为该采购单的入库人。

#### Scenario: 入库操作自动记录入库人

- **WHEN** 用户对状态为「已采购」的采购单执行入库操作（`POST /api/stock-records`，type=1）
- **THEN** 系统创建入库记录后，将关联 PO 的 `inbound_operator_id` 设置为 `LoginContextHolder.getUserId()`，状态变更为「已到货」

#### Scenario: 入库人姓名解析

- **WHEN** 查询采购单列表或详情
- **THEN** 系统通过 `UserRepository.findById(inboundOperatorId)` 解析用户名，优先取 `user.getName()`，降级取 `user.getUsername()`，在响应中返回 `inboundOperatorName` 字段

#### Scenario: 入库人不存在

- **WHEN** `inbound_operator_id` 对应的用户已被删除或不存在
- **THEN** 系统返回 `inboundOperatorId` 原值，`inboundOperatorName` 为 null

### Requirement: 数据库存储

系统 SHALL 在 `t_purchase_order` 表中新增 `purchaser_id` 和 `inbound_operator_id` 两个可为空的 BIGINT 列。

#### Scenario: 表结构

- **WHEN** 执行数据库迁移
- **THEN** `t_purchase_order` 表新增 `purchaser_id BIGINT COMMENT '采购人ID'` 和 `inbound_operator_id BIGINT COMMENT '入库人ID'`，默认值为 NULL
