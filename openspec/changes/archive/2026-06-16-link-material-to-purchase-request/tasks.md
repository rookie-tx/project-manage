## 1. 数据库变更

- [x] 1.1 `t_material` 表：spec 改为 `NOT NULL`，unit 从 `VARCHAR(20)` 改为 `SMALLINT NOT NULL`，更新注释
- [x] 1.2 `t_purchase_request` 表：新增 `material_id BIGINT NOT NULL` 列 + 索引 + 外键约束（使用 `proc_add_column_if_not_exists` 保证幂等）

## 2. 后端 — 物料模块适配 unit 枚举

- [x] 2.1 `Material.java`：unit 字段从 `String` 改为 `Unit` 枚举，更新 `create()` 和 `fromPersistence()` 工厂方法
- [x] 2.2 `MaterialPO.java`：unit 字段从 `String` 改为 `Integer`
- [x] 2.3 `MaterialPoMapper.java`：`toDomain` 方法处理 unit 码值 → 枚举转换，`toPO` 方法处理枚举 → 码值转换
- [x] 2.4 `MaterialMapper.xml`：更新 unit 字段类型映射（MyBatis 自动处理，无需改动）
- [x] 2.5 `CreateMaterialRequest.java`：spec 加 `@NotBlank`，unit 改为 `@NotNull Integer`
- [x] 2.6 `UpdateMaterialRequest.java`：spec 加 `@NotBlank`，unit 改为 `@NotNull Integer`

## 3. 后端 — 采购申请模块适配 materialId

- [x] 3.1 `PurchaseRequest.java`：新增 `materialId` 字段，更新 `create()` 和 `fromPersistence()` 工厂方法
- [x] 3.2 `PurchaseRequestPO.java`：新增 `materialId` 字段
- [x] 3.3 `PurchaseRequestMapper.xml`：所有 SQL 新增 `material_id` 列（insert、select、update）
- [x] 3.4 `PurchaseRequestPoMapper.java`：`toDomain` 和 `toPO` 方法适配 materialId
- [x] 3.5 `CreatePurchaseRequestRequest.java`：新增 `@NotNull materialId`，移除 `name`/`specification`/`unit` 字段
- [x] 3.6 `UpdatePurchaseRequestRequest.java`：新增 `materialId` 字段，移除 `name`/`specification`/`unit` 字段
- [x] 3.7 `PurchaseRequestResponse.java`：新增 `materialId`、`materialName`、`materialSpec`、`materialUnit` 字段
- [x] 3.8 `PurchaseRequestDtoMapper.java`：适配新增和变更字段的映射
- [x] 3.9 `PurchaseRequestApplicationService.java`：创建/更新时根据 materialId 查询物料表，自动填入 name/spec/unit
- [x] 3.10 `ErrorCode.java`：复用已有 MATERIAL_NOT_FOUND (7012)

## 4. Web 前端 — 物料管理表单

- [x] 4.1 `MaterialListPage.vue`：spec 加必填校验，unit 从 Input 改为 Select 下拉（枚举选项），加必填校验
- [x] 4.2 `MaterialListPage.vue`：表格 unit 列显示中文标签，新增 `getUnitLabel()` 方法

## 5. Web 前端 — 采购申请表单

- [x] 5.1 `PurchaseRequestForm.vue`：物料名称从 Input 改为 Select（调用 `/api/materials` 加载物料列表），支持搜索过滤
- [x] 5.2 `PurchaseRequestForm.vue`：选择物料后规格和单位自动填入并设为只读（disabled 样式）
- [x] 5.3 `PurchaseRequestForm.vue`：去掉 name/specification/unit 的表单校验规则，新增 materialId 校验
- [x] 5.4 `PurchaseRequestForm.vue`：`buildSubmitData()` 移除 name/specification/unit，新增 materialId
- [x] 5.5 `PurchaseRequestDetailPage.vue`：已展示物料名称/规格/单位（通过现有字段即可）
- [x] 5.6 `purchaseRequest.js` store：无需改动（透传表单数据）

## 6. Web 前端 — 入库弹窗

- [x] 6.1 `PurchaseOrderListPage.vue`：入库 Modal 中物料 Select 改为只读文本展示，值从 PO 关联的 PR 物料信息获取
- [x] 6.2 入库提交时 materialId 从 PR 的 materialId 自动填入

## 7. 小程序 — 物料管理 API 层

- [x] 7.1 新建 `api/material.js`：封装 `list`、`detail`、`create`、`update`、`del` 函数

## 8. 小程序 — 物料列表页

- [x] 8.1 新建 `pages/material/list.js`：加载物料列表、权限控制（material:create/material:delete）
- [x] 8.2 新建 `pages/material/list.wxml`：物料卡片列表（编码、名称、规格、单位）、空状态、新建按钮
- [x] 8.3 新建 `pages/material/list.wxss`：列表页样式

## 9. 小程序 — 物料表单页

- [x] 9.1 新建 `pages/material/form.js`：新建/编辑模式、单位 picker、必填校验、提交逻辑
- [x] 9.2 新建 `pages/material/form.wxml`：表单 UI（编码、名称、规格、单位 picker）
- [x] 9.3 新建 `pages/material/form.wxss`：表单页样式
- [x] 9.4 新建 `pages/material/form.json`：页面配置

## 10. 小程序 — 采购申请表单适配

- [x] 10.1 `pages/purchase/form.js`：物料从输入框改为 picker（加载 `/api/materials`），选择后自动填入规格和单位（只读）
- [x] 10.2 `pages/purchase/form.js`：提交 payload 移除 name/specification/unit，新增 materialId
- [x] 10.3 `pages/purchase/form.wxml`：物料改为 picker，规格和单位改为只读文本展示
- [x] 10.4 `pages/purchase/detail.wxml`：已通过现有字段展示物料规格和单位

## 11. 小程序 — 入库页面适配

- [x] 11.1 `pages/purchase-order/inbound.js`：物料从手动选择改为从 URL 参数自动带入，移除物料 picker 逻辑
- [x] 11.2 `pages/purchase-order/inbound.wxml`：物料改为只读文本展示

## 12. 小程序 — 注册与入口

- [x] 12.1 `app.json`：注册物料管理页面路由 `pages/material/list` 和 `pages/material/form`
- [x] 12.2 Dashboard 页面新增"物料管理"功能入口（权限控制 `material:read`）

## 额外改动

- [x] `Unit.java`：添加 `@JsonValue` 注解确保 JSON 序列化为整数码值
- [x] `MaterialController.java`：create/update 方法适配 unit Integer → Unit 枚举转换
- [x] `PurchaseOrderResponse.PurchaseRequestInfo`：新增 materialId/materialName/materialSpec 字段
- [x] `PurchaseOrderApplicationService.java`：注入 MaterialRepository，填充物料信息到 PO 响应
- [x] `pages/purchase-order/list.js`：入库导航传递物料参数
- [x] `pages/purchase-order/list.wxml`：入库按钮 data 属性增加物料信息
- [x] `pages/purchase-order/detail.js`：入库导航传递物料参数
