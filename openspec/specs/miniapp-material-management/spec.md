# miniapp-material-management Specification

## Purpose
小程序物料管理功能，提供物料列表浏览、新建/编辑物料、删除物料以及 Dashboard 入口。

## Requirements

### Requirement: 物料列表页

小程序 SHALL 提供物料管理列表页面，支持查看全部物料、下拉刷新和权限控制。

#### Scenario: 首次加载列表

- **WHEN** 已登录用户从小程序 Dashboard 点击"物料管理"入口进入列表页
- **THEN** 页面调用 `GET /api/materials`，以卡片形式展示物料列表，每条显示物料编码、物料名称、规格、单位

#### Scenario: 下拉刷新

- **WHEN** 用户在列表顶部下拉触发刷新
- **THEN** 页面重新请求最新物料数据

#### Scenario: 无数据

- **WHEN** 物料列表返回空数组
- **THEN** 页面显示"暂无物料数据"的空状态提示

#### Scenario: 权限控制

- **WHEN** 用户没有 `material:create` 权限
- **THEN** 页面不显示"新建"按钮

### Requirement: 新建/编辑物料

小程序 SHALL 提供物料表单页面，通过 `id` 参数区分新建和编辑模式，规格和单位为必填。

#### Scenario: 新建模式

- **WHEN** 用户从物料列表页点击"新建"按钮进入 `/pages/material/form`（无 `id` 参数）
- **THEN** 页面展示空白表单（物料编码、物料名称、规格、单位），标题为"新建物料"，提交时调用 `POST /api/materials`

#### Scenario: 编辑模式

- **WHEN** 用户从物料列表页点击"编辑"按钮进入 `/pages/material/form?id=123`
- **THEN** 页面调用 `GET /api/materials/123` 加载已有数据并预填到表单，标题为"编辑物料"，提交时调用 `PUT /api/materials/123`

#### Scenario: 单位选择

- **WHEN** 用户点击"单位"选择器
- **THEN** 弹出单位列表（个、台、套、米、吨、立方米、卷、桶、箱、其他），显示当前选中的单位名称

#### Scenario: 必填校验

- **WHEN** 用户提交表单但缺少物料编码、物料名称、规格或单位
- **THEN** 页面显示对应的校验错误提示，不发起提交请求

#### Scenario: 成功提交

- **WHEN** 用户填写完所有必填字段并点击提交
- **THEN** 调用对应 API，成功后显示 Toast，返回上一页并通知列表页刷新

### Requirement: 物料删除

小程序 SHALL 支持在物料列表页删除物料。

#### Scenario: 删除确认

- **WHEN** 用户点击某条物料的"删除"按钮
- **THEN** 弹出确认弹窗，确认后调用 `DELETE /api/materials/{id}`，成功后刷新列表

#### Scenario: 删除权限控制

- **WHEN** 用户没有 `material:delete` 权限
- **THEN** 列表页不显示"删除"按钮

### Requirement: Dashboard 物料管理入口

小程序 Dashboard SHALL 新增物料管理功能入口。

#### Scenario: 点击物料管理入口

- **WHEN** 已登录用户在 Dashboard 功能入口网格中点击"物料管理"
- **THEN** 跳转到物料列表页 `/pages/material/list`

#### Scenario: 入口展示

- **WHEN** 用户查看 Dashboard 功能入口网格
- **THEN** 物料管理入口显示图标和名称"物料管理"
