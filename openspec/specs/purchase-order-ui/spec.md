## ADDED Requirements

### Requirement: 采购管理列表页

系统 SHALL 在 `/purchase-orders` 路由提供采购记录列表，支持状态筛选和分页。

### Requirement: 新建采购记录

系统 SHALL 在 `/purchase-orders/new` 路由提供新建表单，选择已通过的采购申请，填写供应商信息和价格。

### Requirement: 采购记录详情

系统 SHALL 在 `/purchase-orders/:id` 路由展示采购记录详情，含关联文件展示和状态操作。

### Requirement: 导航菜单入口

系统 SHALL 在 Dashboard 导航菜单新增"采购管理"菜单项。
