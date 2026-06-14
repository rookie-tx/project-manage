## MODIFIED Requirements

### Requirement: 采购管理页 Tab 切换
系统 SHALL 在 Web 端采购管理页提供 Tab 切换（待采购 / 采购中 / 已采购 / 已到货）。

#### Scenario: 待采购 Tab
- **WHEN** 用户点击"待采购"Tab
- **THEN** 系统展示状态为"采购中"且供应商名称为空的 PO 列表，每条可点击"编辑"弹出 Modal 补充供应商和价格信息

#### Scenario: 采购中 Tab
- **WHEN** 用户点击"采购中"Tab
- **THEN** 系统展示已补填供应商信息的"采购中"PO 列表，每条可点击"采购完成"将状态流转为"已采购"

#### Scenario: 已采购 Tab
- **WHEN** 用户点击"已采购"Tab
- **THEN** 系统展示状态为"已采购"的 PO 列表，每条可点击"入库"弹出 Modal 选择仓库和物料

#### Scenario: 已到货 Tab
- **WHEN** 用户点击"已到货"Tab
- **THEN** 系统展示状态为"已到货"的 PO 列表（只读）

### Requirement: 采购管理页操作权限
系统 SHALL 根据操作所需权限和 PO 状态控制按钮可见性。

#### Scenario: 编辑按钮
- **WHEN** PO 状态为"采购中"（PURCHASING）且用户有 `purchase:update` 权限
- **THEN** 显示"编辑"按钮

#### Scenario: 采购完成按钮
- **WHEN** PO 状态为"采购中"且供应商信息已填写，用户有 `purchase:update` 权限
- **THEN** 显示"采购完成"按钮

#### Scenario: 入库按钮
- **WHEN** PO 状态为"已采购"（PURCHASED）且用户有 `stock:in` 权限
- **THEN** 显示"入库"按钮
