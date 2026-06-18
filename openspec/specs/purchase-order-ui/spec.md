# purchase-order-ui Specification

## Purpose
TBD - created by archiving change integrate-flowable-approval. Updated by syncing change optimize-purchase-flow. Updated by syncing change split-procurement-menus. Updated by change add-purchase-personnel-fields.

## Requirements

### Requirement: 采购管理列表页
系统 SHALL 在 `/purchase-orders` 路由提供采购汇总页，展示全部状态的采购单列表（待采购、已采购、已到货），支持分页；同时在 `/purchase-orders/pending`、`/purchase-orders/purchased`、`/purchase-orders/arrived` 分别提供按状态过滤的采购单列表。

### Requirement: 采购记录详情
系统 SHALL 在 `/purchase-orders/:id` 路由展示采购记录详情，含关联文件展示和状态操作。

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

### Requirement: 导航菜单入口
系统 SHALL 在 Dashboard 导航菜单中提供"采购汇总"、"待采购"、"已采购"、"已到货"四个独立菜单项，分别绑定权限码 `purchase:read`、`purchase:pending:read`、`purchase:purchased:read`、`purchase:arrived:read`。

### Requirement: 采购管理页操作权限
系统 SHALL 根据操作所需权限和采购单状态控制按钮可见性。

#### Scenario: 采购完成按钮
- **WHEN** 在待采购页面中，采购单 status=0 且用户有 `purchase:update` 权限
- **THEN** 显示"采购完成"按钮

#### Scenario: 入库按钮
- **WHEN** 在已采购页面中，采购单 status=1 且用户有 `stock:in` 权限
- **THEN** 显示"入库"按钮

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

### Requirement: 待采购详情页报价展示

系统 SHALL 在待采购详情页中展示供应商报价列表区域，并支持选用报价操作。

#### Scenario: 报价列表区域展示

- **WHEN** 管理员进入待采购详情页（`/purchase-orders/:id/edit` 或同等详情视图）
- **THEN** 页面包含「供应商报价」区域，以表格展示所有报价：供应商公司名、联系人、联系电话、单价、总金额、交货日期、备注、报价时间、状态标签

#### Scenario: 选用报价按钮

- **WHEN** 报价状态为 PENDING 且当前用户有 `purchase:update` 权限
- **THEN** 该报价行展示「选用」按钮

#### Scenario: 选用确认对话框

- **WHEN** 管理员点击「选用」按钮
- **THEN** 系统弹出确认对话框，包含供应商名称和报价金额摘要，提供「确认选用」和「取消」按钮

#### Scenario: 选用成功反馈

- **WHEN** 管理员确认选用报价
- **THEN** 系统显示成功提示，采购单信息自动更新（供应商名称、电话、单价、总金额），页面状态刷新

#### Scenario: 无报价提示

- **WHEN** 当前采购单没有任何供应商报价
- **THEN** 报价区域显示「暂无供应商报价」的空状态提示
