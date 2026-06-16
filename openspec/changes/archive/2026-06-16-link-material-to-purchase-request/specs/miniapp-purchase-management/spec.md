## MODIFIED Requirements

### Requirement: 新建/编辑采购申请

小程序 SHALL 提供采购申请表单页面，物料从物料库 picker 选择，规格和单位自动带出且不可编辑。

#### Scenario: 新建模式

- **WHEN** 用户从列表页点击"新建"按钮进入 `/pages/purchase/form`（无 `id` 参数）
- **THEN** 页面展示空白表单（项目、物料、品牌、数量、到场时间、备注），标题为"新建采购申请"，提交时调用 `POST /api/purchase-requests`

#### Scenario: 编辑模式

- **WHEN** 用户从列表或详情页点击"编辑"按钮进入 `/pages/purchase/form?id=123`
- **THEN** 页面调用 `GET /api/purchase-requests/123` 加载已有数据并预填到表单，标题为"编辑采购申请"，提交时调用 `PUT /api/purchase-requests/123`

#### Scenario: 物料选择

- **WHEN** 用户点击"物料"选择器
- **THEN** 弹出物料名称列表（数据来自 `GET /api/materials`），每条显示物料编码和名称，用户选择后自动填入物料

#### Scenario: 物料选择后自动带出规格和单位

- **WHEN** 用户选中某个物料
- **THEN** 规格和单位字段自动填入该物料的 spec 和 unit 值，且变为只读（不可手动修改）

#### Scenario: 编辑模式下物料可切换

- **WHEN** 用户在编辑页重新选择物料
- **THEN** 规格和单位自动更新为新物料的对应值，但仍为只读状态

#### Scenario: 项目选择

- **WHEN** 用户点击"所属项目"选择器
- **THEN** 弹出项目名称列表（数据来自 `GET /api/projects/select`），用户选择后显示所选项目名称

#### Scenario: 必填校验

- **WHEN** 用户提交表单但缺少项目、物料、数量或到场时间
- **THEN** 页面显示对应的校验错误提示，不发起提交请求

#### Scenario: 成功提交

- **WHEN** 用户填写完所有必填字段并点击提交
- **THEN** 调用对应 API，成功后显示 Toast，返回上一页并通知列表页刷新

### Requirement: 采购申请详情

小程序 SHALL 提供采购申请详情页，展示完整信息（含物料规格和单位）和审批功能。

#### Scenario: 成功加载详情

- **WHEN** 用户从列表页点击进入详情页
- **THEN** 页面调用 `GET /api/purchase-requests/{id}`，展示项目名称、物料名称、物料规格、物料单位、品牌、数量、到场时间、状态、备注、创建人、创建时间、更新时间
