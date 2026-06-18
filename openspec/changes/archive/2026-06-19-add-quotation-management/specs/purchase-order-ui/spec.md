# purchase-order-ui Delta Specification

## ADDED Requirements

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
