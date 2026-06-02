## ADDED Requirements

### Requirement: 仓库管理页面
系统 SHALL 在 `/warehouses` 路由提供仓库管理页面，表格展示仓库列表，支持创建/编辑弹窗和删除。

#### Scenario: 显示仓库列表
- **WHEN** 拥有 warehouse:read 权限的用户导航到 `/warehouses`
- **THEN** 页面显示仓库表格，列含编码、名称、位置、状态、操作按钮

#### Scenario: 创建仓库弹窗
- **WHEN** 用户点击"创建仓库"
- **THEN** 弹出表单含编码、名称、位置输入框

### Requirement: 物料管理页面
系统 SHALL 在 `/materials` 路由提供物料管理页面，表格展示物料列表含库存量，支持创建/编辑弹窗。

#### Scenario: 显示物料列表
- **WHEN** 拥有 material:read 权限的用户导航到 `/materials`
- **THEN** 页面显示物料表格，列含编码、名称、规格、单位、库存量、操作

### Requirement: 出入库记录页面
系统 SHALL 在 `/stock-records` 路由提供出入库流水页面，支持筛选和新增入库/出库。

#### Scenario: 筛选记录
- **WHEN** 用户选择仓库和类型筛选条件
- **THEN** 表格显示匹配的流水记录

#### Scenario: 入库操作
- **WHEN** 拥有 stock:in 权限的用户点击"入库"
- **THEN** 弹出表单选择仓库、物料、数量，可关联采购单

#### Scenario: 出库操作
- **WHEN** 拥有 stock:out 权限的用户点击"出库"
- **THEN** 弹出表单选择仓库、物料、数量，可关联项目

### Requirement: 菜单可见性
系统 SHALL 在顶部导航新增"仓库管理"菜单项，warehouse:read 权限可见，点击进入 `/warehouses`。
