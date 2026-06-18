# purchase-inbound Specification

## Purpose
TBD - created by syncing change optimize-purchase-flow. Updated by change add-purchase-personnel-fields.

## Requirements

### Requirement: 待入库列表查询

系统 SHALL 在 `GET /api/purchase-orders/pending-inbound` 端点返回所有状态为「已采购」的 PO 列表，供入库管理页使用。

#### Scenario: 有待入库记录

- **WHEN** 存在状态为「已采购」的采购记录
- **THEN** 系统返回记录列表，每条包含 PO ID、关联的 PR 物料名称、物料规格、物料单位、数量

#### Scenario: 无待入库记录

- **WHEN** 不存在「已采购」状态的采购记录
- **THEN** 系统返回空列表

### Requirement: 入库完成联动

系统 SHALL 在入库操作时同步更新关联 PO 的状态为「已到货」并记录入库人，物料从采购申请自动带入。

#### Scenario: 入库完成

- **WHEN** 用户对一条「已采购」状态的 PO 执行入库操作（选择仓库、确认数量，调用 `POST /api/stock-records`）
- **THEN** 系统根据关联 PR 的 materialId 自动填入物料信息，创建入库记录（StockRecord），同时将 PO 状态更新为「已到货」，并将 PO 的 `inbound_operator_id` 设置为当前登录用户

#### Scenario: 重复入库

- **WHEN** 用户对已经是「已到货」状态的 PO 再次执行入库
- **THEN** 系统返回状态流转不合法错误

### Requirement: 入库操作弹窗

系统 SHALL 在入库操作弹窗中自动填入采购申请关联的物料信息，物料字段只读不可修改。

#### Scenario: Web 端入库弹窗

- **WHEN** 用户点击某条 PO 的「入库」按钮
- **THEN** 系统弹出 Modal，显示关联 PR 的物料名称、规格、单位（只读），用户只需选择仓库和确认数量后提交

#### Scenario: 小程序端入库页

- **WHEN** 用户从小程序进入入库页
- **THEN** 页面展示关联 PR 的物料名称、规格、单位（只读文本），用户只需选择仓库和输入数量后提交

### Requirement: 入库管理页展示待入库 PO
系统 SHALL 在 Web 端入库管理页展示待入库的 PO 列表，用户可选择一条记录执行入库操作。

#### Scenario: 入库管理页看到待入库列表
- **WHEN** 用户访问入库管理页
- **THEN** 系统展示「待入库」区域的已采购 PO 列表，每条可点击「入库」

### Requirement: 入库操作记录操作人
系统 SHALL 在入库操作完成后，将当前登录用户 ID 写入关联采购单的 `inbound_operator_id` 字段。

#### Scenario: Web 端入库记录入库人
- **WHEN** 用户在 Web 端已采购页面点击「入库」按钮并提交入库表单
- **THEN** 系统在创建 StockRecord 后，将关联 PO 的 `inbound_operator_id` 更新为 `LoginContextHolder.getUserId()`

#### Scenario: 小程序端入库记录入库人
- **WHEN** 用户在小程序入库页点击「确认入库」并提交
- **THEN** 系统在创建 StockRecord 后，将关联 PO 的 `inbound_operator_id` 更新为 `LoginContextHolder.getUserId()`
