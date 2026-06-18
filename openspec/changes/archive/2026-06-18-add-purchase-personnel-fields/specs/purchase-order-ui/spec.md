# purchase-order-ui Delta Specification

## MODIFIED Requirements

### Requirement: 采购管理页 Tab 切换

系统 SHALL 将原有单一页面内的 Tab 切换改为四个独立菜单和路由：采购汇总（`/purchase-orders`）、待采购（`/purchase-orders/pending`）、已采购（`/purchase-orders/purchased`）、已到货（`/purchase-orders/arrived`），每个页面独立展示对应状态的列表。表格 SHALL 包含采购人列和入库人列。

#### Scenario: 待采购页面

- **WHEN** 用户导航到 `/purchase-orders/pending`
- **THEN** 系统展示 status=0 的采购单列表，每条记录可点击「采购完成」按钮弹出内联 Modal 填写供应商和价格信息，确认后流转为「已采购」并自动记录当前用户为采购人

#### Scenario: 已采购页面

- **WHEN** 用户导航到 `/purchase-orders/purchased`
- **THEN** 系统展示 status=1 的采购单列表（含采购人列），每条记录可点击「入库」按钮弹出内联 Modal 选择仓库和物料，确认后创建入库记录并流转为「已到货」，自动记录当前用户为入库人

#### Scenario: 已到货页面

- **WHEN** 用户导航到 `/purchase-orders/arrived`
- **THEN** 系统展示 status=2 的采购单列表（含采购人列和入库人列，只读，无操作按钮）

#### Scenario: 采购汇总页面

- **WHEN** 用户导航到 `/purchase-orders`
- **THEN** 系统展示全部状态的采购单列表，每条记录显示其当前状态标签、采购人、入库人

## ADDED Requirements

### Requirement: 表格采购人列

系统 SHALL 在所有采购单列表表格中展示采购人列，显示 `purchaserName`。

#### Scenario: 采购人已记录

- **WHEN** 采购单的 `purchaserName` 不为空
- **THEN** 表格「采购人」列显示用户名

#### Scenario: 采购人未记录

- **WHEN** 采购单的 `purchaserName` 为空（尚未采购完成）
- **THEN** 表格「采购人」列显示「-」

### Requirement: 表格入库人列

系统 SHALL 在所有采购单列表表格中展示入库人列，显示 `inboundOperatorName`。

#### Scenario: 入库人已记录

- **WHEN** 采购单的 `inboundOperatorName` 不为空
- **THEN** 表格「入库人」列显示用户名

#### Scenario: 入库人未记录

- **WHEN** 采购单的 `inboundOperatorName` 为空（尚未入库）
- **THEN** 表格「入库人」列显示「-」
