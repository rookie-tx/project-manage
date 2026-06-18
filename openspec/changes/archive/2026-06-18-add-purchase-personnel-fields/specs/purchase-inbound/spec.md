# purchase-inbound Delta Specification

## MODIFIED Requirements

### Requirement: 入库完成联动

系统 SHALL 在入库操作时同步更新关联 PO 的状态为「已到货」并记录入库人，物料从采购申请自动带入。

#### Scenario: 入库完成

- **WHEN** 用户对一条「已采购」状态的 PO 执行入库操作（选择仓库、确认数量，调用 `POST /api/stock-records`）
- **THEN** 系统根据关联 PR 的 materialId 自动填入物料信息，创建入库记录（StockRecord），同时将 PO 状态更新为「已到货」，并将 PO 的 `inbound_operator_id` 设置为当前登录用户

#### Scenario: 重复入库

- **WHEN** 用户对已经是「已到货」状态的 PO 再次执行入库
- **THEN** 系统返回状态流转不合法错误

## ADDED Requirements

### Requirement: 入库操作记录操作人

系统 SHALL 在入库操作完成后，将当前登录用户 ID 写入关联采购单的 `inbound_operator_id` 字段。

#### Scenario: Web 端入库记录入库人

- **WHEN** 用户在 Web 端已采购页面点击「入库」按钮并提交入库表单
- **THEN** 系统在创建 StockRecord 后，将关联 PO 的 `inbound_operator_id` 更新为 `LoginContextHolder.getUserId()`

#### Scenario: 小程序端入库记录入库人

- **WHEN** 用户在小程序入库页点击「确认入库」并提交
- **THEN** 系统在创建 StockRecord 后，将关联 PO 的 `inbound_operator_id` 更新为 `LoginContextHolder.getUserId()`
