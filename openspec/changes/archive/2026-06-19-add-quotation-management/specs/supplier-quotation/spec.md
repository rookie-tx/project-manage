# supplier-quotation Specification

## Purpose

供应商端报价功能（小程序），供应商可浏览待报价物料、提交报价、管理自己的报价。

## ADDED Requirements

### Requirement: 待报价物料列表页

系统 SHALL 在小程序中提供"待报价"页面，展示所有 PURCHASING 状态的采购单供供应商浏览和报价。

#### Scenario: 查看待报价物料

- **WHEN** 供应商用户进入"待报价"页面
- **THEN** 系统以列表形式展示所有待采购物料，每条显示物料名称、规格、数量、单位、项目名称、预计到货日期，并提供"报价"入口按钮

#### Scenario: 已报价标记

- **WHEN** 供应商已对某物料提交过报价
- **THEN** 该物料在列表中标记为"已报价"，按钮文案变为"修改报价"

#### Scenario: 空列表

- **WHEN** 当前无待采购物料
- **THEN** 系统展示空状态提示「暂无可报价的物料」

### Requirement: 报价表单页

系统 SHALL 在小程序中提供报价表单，供应商可填写单价、总金额、预计交货日期和备注。

#### Scenario: 首次报价

- **WHEN** 供应商进入未报价物料的报价表单页，填写单价、总金额、交货日期、备注并提交
- **THEN** 系统创建报价记录，提示「报价成功」，返回待报价列表

#### Scenario: 修改报价

- **WHEN** 供应商进入已报价物料的报价表单页，修改价格或交货日期并提交
- **THEN** 系统更新已有报价，提示「报价已更新」，返回待报价列表

#### Scenario: 表单校验

- **WHEN** 供应商未填写必填字段（单价或总金额）即提交
- **THEN** 系统提示「请填写必填项」，阻止提交

### Requirement: 我的报价列表页

系统 SHALL 在小程序中提供"我的报价"页面，供应商可查看自己提交的所有报价记录。

#### Scenario: 查看我的报价

- **WHEN** 供应商进入"我的报价"页面
- **THEN** 系统展示该供应商提交的所有报价记录，每条显示物料名称、报价金额、提交时间、报价状态（待审核/已选用）

#### Scenario: 点击查看详情

- **WHEN** 供应商点击某条报价记录
- **THEN** 系统展示该报价的完整信息和关联的采购物料详情

#### Scenario: 报价状态标签

- **WHEN** 报价已被管理员选用
- **THEN** 状态标签显示「已选用」并以高亮样式区分

### Requirement: 小程序首页入口

系统 SHALL 在小程序首页 Dashboard 中提供"待报价"和"我的报价"功能入口。

#### Scenario: 供应商角色可见

- **WHEN** 供应商角色用户登录小程序
- **THEN** Dashboard 显示"待报价"和"我的报价"入口模块

#### Scenario: 非供应商角色不可见

- **WHEN** 非供应商角色用户登录小程序
- **THEN** Dashboard 不显示报价相关入口模块

### Requirement: 报价相关 API 调用

系统 SHALL 在小程序中新增 `api/quotation.js` 模块，封装报价相关 HTTP 请求。

#### Scenario: API 方法清单

- **WHEN** 小程序需要调用报价相关接口
- **THEN** 通过 `api/quotation.js` 调用以下方法：`getPendingOrders()`（待报价列表）、`getPendingOrderDetail(id)`（详情）、`submitQuotation(data)`（提交报价）、`getMyQuotations(params)`（我的报价列表）、`getMyQuotationDetail(id)`（我的报价详情）
