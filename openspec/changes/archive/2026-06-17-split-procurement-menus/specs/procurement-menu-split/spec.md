## ADDED Requirements

### Requirement: 采购汇总菜单

系统 SHALL 在 Dashboard 导航菜单中提供"采购汇总"菜单项，点击后展示全部状态的采购单列表（待采购、已采购、已到货），权限码为 `purchase:read`。

#### Scenario: 用户点击采购汇总菜单
- **WHEN** 用户拥有 `purchase:read` 权限且点击导航菜单中的"采购汇总"
- **THEN** 系统导航到 `/purchase-orders`，展示不分状态过滤的全部采购单列表

#### Scenario: 无权限用户不显示菜单
- **WHEN** 用户没有 `purchase:read` 权限
- **THEN** 导航菜单中不显示"采购汇总"菜单项

### Requirement: 待采购菜单

系统 SHALL 在 Dashboard 导航菜单中提供"待采购"菜单项，点击后展示状态为"采购中"（status=0）的采购单列表，权限码为 `purchase:pending:read`。

#### Scenario: 用户点击待采购菜单
- **WHEN** 用户拥有 `purchase:pending:read` 权限且点击导航菜单中的"待采购"
- **THEN** 系统导航到 `/purchase-orders/pending`，展示 status=0 的采购单列表

#### Scenario: 待采购列表操作-采购完成
- **WHEN** 用户在待采购列表中点击某条记录的"采购完成"按钮
- **THEN** 系统弹出内联 Modal，包含供应商名称、供应商电话、单价、总金额、采购日期、预计到货日期、备注字段，确认后更新采购单信息并将状态流转为"已采购"（status=1）

### Requirement: 已采购菜单

系统 SHALL 在 Dashboard 导航菜单中提供"已采购"菜单项，点击后展示状态为"已采购"（status=1）的采购单列表，权限码为 `purchase:purchased:read`。

#### Scenario: 用户点击已采购菜单
- **WHEN** 用户拥有 `purchase:purchased:read` 权限且点击导航菜单中的"已采购"
- **THEN** 系统导航到 `/purchase-orders/purchased`，展示 status=1 的采购单列表

#### Scenario: 已采购列表操作-入库
- **WHEN** 用户在已采购列表中点击某条记录的"入库"按钮且用户拥有 `stock:in` 权限
- **THEN** 系统弹出内联 Modal，包含仓库选择、物料信息（从关联采购申请自动带入）、数量字段，确认后创建入库记录并流转状态为"已到货"（status=2）

### Requirement: 已到货菜单

系统 SHALL 在 Dashboard 导航菜单中提供"已到货"菜单项，点击后展示状态为"已到货"（status=2）的采购单列表，权限码为 `purchase:arrived:read`。

#### Scenario: 用户点击已到货菜单
- **WHEN** 用户拥有 `purchase:arrived:read` 权限且点击导航菜单中的"已到货"
- **THEN** 系统导航到 `/purchase-orders/arrived`，展示 status=2 的采购单只读列表（无操作按钮）

### Requirement: 移除手动新建采购单入口

系统 SHALL 移除 `/purchase-orders/new` 路由和"新建采购记录"按钮，采购单改为由后端在采购申请审批通过后自动创建。

#### Scenario: 用户无法访问新建页面
- **WHEN** 用户尝试访问 `/purchase-orders/new` 路由
- **THEN** 系统返回 404 或重定向到采购汇总页
