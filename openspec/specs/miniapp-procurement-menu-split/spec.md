# miniapp-procurement-menu-split Specification

## Purpose
小程序端采购管理入口拆分——将原有单一"采购管理"功能入口拆分为采购汇总、待采购、已采购、已到货四个独立 Dashboard 入口，每个入口对应独立页面。

## Requirements

### Requirement: 采购汇总入口

小程序 Dashboard SHALL 提供"采购汇总"功能入口，点击后跳转到汇总页展示全部状态的采购单列表，权限码为 `purchase:read`。

#### Scenario: 用户点击采购汇总入口
- **WHEN** 用户拥有 `purchase:read` 权限且点击 Dashboard 中的"采购汇总"
- **THEN** 系统导航到 `/pages/purchase-order/list`，展示不分状态过滤的全部采购单列表

#### Scenario: 无权限用户不显示入口
- **WHEN** 用户没有 `purchase:read` 权限
- **THEN** Dashboard 功能网格中不显示"采购汇总"入口

### Requirement: 待采购入口

小程序 Dashboard SHALL 提供"待采购"功能入口，点击后跳转到待采购独立页面，展示 status=0 的采购单列表，权限码为 `purchase:pending:read`。

#### Scenario: 用户点击待采购入口
- **WHEN** 用户拥有 `purchase:pending:read` 权限且点击 Dashboard 中的"待采购"
- **THEN** 系统导航到 `/pages/purchase-order/pending`，展示 status=0 的采购单列表

#### Scenario: 待采购列表操作-采购完成
- **WHEN** 用户在待采购列表中点击某条记录的"采购完成"按钮
- **THEN** 系统导航到 `/pages/purchase-order/form?id=<id>`，填写供应商信息后流转到已采购

### Requirement: 已采购入口

小程序 Dashboard SHALL 提供"已采购"功能入口，点击后跳转到已采购独立页面，展示 status=1 的采购单列表，权限码为 `purchase:purchased:read`。

#### Scenario: 用户点击已采购入口
- **WHEN** 用户拥有 `purchase:purchased:read` 权限且点击 Dashboard 中的"已采购"
- **THEN** 系统导航到 `/pages/purchase-order/purchased`，展示 status=1 的采购单列表

#### Scenario: 已采购列表操作-入库
- **WHEN** 用户在已采购列表中点击某条记录的"入库"按钮
- **THEN** 系统导航到 `/pages/purchase-order/inbound?id=<id>` 进行入库操作

### Requirement: 已到货入口

小程序 Dashboard SHALL 提供"已到货"功能入口，点击后跳转到已到货独立页面，展示 status=2 的采购单只读列表，权限码为 `purchase:arrived:read`。

#### Scenario: 用户点击已到货入口
- **WHEN** 用户拥有 `purchase:arrived:read` 权限且点击 Dashboard 中的"已到货"
- **THEN** 系统导航到 `/pages/purchase-order/arrived`，展示 status=2 的采购单只读列表（无操作按钮）

### Requirement: 页面必须注册到 app.json

小程序 SHALL 在 `app.json` 的 `pages` 数组中注册 `pages/purchase-order/pending`、`pages/purchase-order/purchased`、`pages/purchase-order/arrived` 三个新页面。

#### Scenario: 页面正常访问
- **WHEN** 用户导航到新注册的待采购/已采购/已到货页面
- **THEN** 小程序正常渲染对应页面，不报页面未注册错误
