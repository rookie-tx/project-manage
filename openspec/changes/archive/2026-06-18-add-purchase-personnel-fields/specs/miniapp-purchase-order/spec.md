# miniapp-purchase-order Delta Specification

## MODIFIED Requirements

### Requirement: 采购管理列表

小程序 SHALL 提供四个独立采购记录列表页——汇总页展示全部状态、待采购页展示 status=0、已采购页展示 status=1、已到货页展示 status=2——每个页面不再包含 Tab 切换，独立支持上拉加载更多和下拉刷新。列表 SHALL 展示采购人和入库人信息。

#### Scenario: 待采购页

- **WHEN** 用户从 Dashboard 进入「待采购」
- **THEN** 导航到 `/pages/purchase-order/pending`，展示 status=0 的采购单列表（采购人、入库人列显示为空），每条可点击「采购完成」跳转 form 页

#### Scenario: 已采购页

- **WHEN** 用户从 Dashboard 进入「已采购」
- **THEN** 导航到 `/pages/purchase-order/purchased`，展示 status=1 的采购单列表（含采购人信息），每条可点击「入库」跳转 inbound 页

#### Scenario: 已到货页

- **WHEN** 用户从 Dashboard 进入「已到货」
- **THEN** 导航到 `/pages/purchase-order/arrived`，展示 status=2 的采购单只读列表（含采购人和入库人信息）

#### Scenario: 汇总页

- **WHEN** 用户从 Dashboard 进入「采购汇总」
- **THEN** 导航到 `/pages/purchase-order/list`，展示全部状态的采购单列表（含采购人和入库人信息）

### Requirement: 采购记录详情

小程序 SHALL 提供采购记录详情页，展示完整信息（含采购人、入库人）和关联文件，支持状态流转操作和文件预览。

#### Scenario: 详情展示人员信息

- **WHEN** 用户进入采购单详情页
- **THEN** 详情页展示「采购人」字段（值为 `purchaserName`，若为空则显示「-」）和「入库人」字段（值为 `inboundOperatorName`，若为空则显示「-」）

## ADDED Requirements

### Requirement: 列表项展示采购人

小程序采购单列表项 SHALL 展示采购人姓名。

#### Scenario: 有采购人

- **WHEN** 采购单的 `purchaserName` 不为空
- **THEN** 列表项显示采购人姓名

#### Scenario: 无采购人

- **WHEN** 采购单的 `purchaserName` 为空
- **THEN** 列表项不显示采购人信息或显示为「-」

### Requirement: 列表项展示入库人

小程序采购单列表项 SHALL 展示入库人姓名。

#### Scenario: 有入库人

- **WHEN** 采购单的 `inboundOperatorName` 不为空
- **THEN** 列表项显示入库人姓名

#### Scenario: 无入库人

- **WHEN** 采购单的 `inboundOperatorName` 为空
- **THEN** 列表项不显示入库人信息或显示为「-」
