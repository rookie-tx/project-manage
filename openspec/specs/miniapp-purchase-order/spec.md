# miniapp-purchase-order Specification

## Purpose
小程序端采购管理规格。Updated by syncing change split-procurement-menus.

## Requirements

### Requirement: 采购管理列表

小程序 SHALL 提供四个独立采购记录列表页——汇总页展示全部状态、待采购页展示 status=0、已采购页展示 status=1、已到货页展示 status=2——每个页面不再包含 Tab 切换，独立支持上拉加载更多和下拉刷新。

#### Scenario: 待采购页
- **WHEN** 用户从 Dashboard 进入"待采购"
- **THEN** 导航到 `/pages/purchase-order/pending`，展示 status=0 的采购单列表，每条可点击"采购完成"跳转 form 页

#### Scenario: 已采购页
- **WHEN** 用户从 Dashboard 进入"已采购"
- **THEN** 导航到 `/pages/purchase-order/purchased`，展示 status=1 的采购单列表，每条可点击"入库"跳转 inbound 页

#### Scenario: 已到货页
- **WHEN** 用户从 Dashboard 进入"已到货"
- **THEN** 导航到 `/pages/purchase-order/arrived`，展示 status=2 的采购单只读列表

#### Scenario: 汇总页
- **WHEN** 用户从 Dashboard 进入"采购汇总"
- **THEN** 导航到 `/pages/purchase-order/list`，展示全部状态的采购单列表

### Requirement: 新建/编辑采购记录

小程序 SHALL 提供采购记录表单页，通过 `id` 参数区分新建和编辑。新建时从已通过的采购申请中选择，填写供应商信息、价格、日期，支持合同附件上传。仅"采购中"状态可编辑。

### Requirement: 采购记录详情

小程序 SHALL 提供采购记录详情页，展示完整信息和关联文件，支持状态流转操作和文件预览。

### Requirement: Dashboard 入口

小程序 Dashboard SHALL 提供"采购汇总"、"待采购"、"已采购"、"已到货"四个独立功能入口，分别绑定权限码 `purchase:read`、`purchase:pending:read`、`purchase:purchased:read`、`purchase:arrived:read`，替代原有的单一"采购管理"入口。
