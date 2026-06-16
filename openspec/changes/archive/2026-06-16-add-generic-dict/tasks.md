## 1. 数据库层

- [x] 1.1 在 `schema-mysql.sql` 中新增 `t_dict` 表 DDL 及 10 条单位种子数据
- [x] 1.2 修改 `t_purchase_request.unit` 列：SMALLINT → VARCHAR(32) NOT NULL COMMENT '单位字典code'
- [x] 1.3 修改 `t_material.unit` 列：INTEGER → VARCHAR(32) NOT NULL COMMENT '单位字典code'

## 2. 后端 — 新增 dict 模块

- [x] 2.1 创建 `DictPO` 持久化对象（`cn.tx.aiagent.dict.infrastructure.DictPO`）
- [x] 2.2 创建 `DictMapper` MyBatis Mapper 接口（`selectByType` 方法）
- [x] 2.3 创建 `DictMapper.xml` SQL 映射文件（查询条件：type + enabled 过滤 + sort 排序）
- [x] 2.4 创建 `DictItem` 领域实体（record 或 @Getter 类，含 code/label/sort）
- [x] 2.5 创建 `DictResponse` DTO（code, label, sort 三个字段）
- [x] 2.6 创建 `DictController`（`GET /api/dicts/{type}`，支持 `enabled` 参数）

## 3. 后端 — 删除 Unit 枚举，修改 PurchaseRequest 模块

- [x] 3.1 删除 `purchaserequest/domain/Unit.java` 枚举类
- [x] 3.2 修改 `PurchaseRequestPO`：`unit` 字段 Integer → String
- [x] 3.3 修改 `PurchaseRequest` 领域实体：`unit` 字段 Unit 枚举 → String，更新 `create()`/`fromPersistence()`/`update()` 签名
- [x] 3.4 修改 `PurchaseRequestPoMapper`：移除 `Unit.fromCode()` 和 `Unit.getCode()`，直接透传 String
- [x] 3.5 修改 `PurchaseRequestResponse`：`unitCode` Integer → String，移除 `unitLabel` 字段
- [x] 3.6 修改 `PurchaseRequestDtoMapper`：适配 unitCode 类型变更
- [x] 3.7 修改 `PurchaseRequestApplicationService`：适配 PurchaseRequest 和 Material 的 unit 类型变更

## 4. 后端 — 修改 Warehouse（Material）模块

- [x] 4.1 修改 `MaterialPO`：`unit` 字段 Integer → String
- [x] 4.2 修改 `Material` 领域实体：`unit` 字段 Unit 枚举 → String，更新 `create()`/`fromPersistence()` 签名
- [x] 4.3 修改 `MaterialPoMapper`：移除 `Unit.fromCode()` 和 `Unit.getCode()`，直接透传 String
- [x] 4.4 修改 `CreateMaterialRequest` / `UpdateMaterialRequest`：`unit` 字段 Integer → String
- [x] 4.5 修改 `MaterialController`：移除 `Unit.fromCode()` 调用

## 5. 后端 — 修改 PurchaseOrder 模块

- [x] 5.1 修改 `PurchaseOrderApplicationService`：`quantityStr` 拼接逻辑中的 `pr.getUnit().getLabel()` 改为直接使用 String code（或前端查 dict 获取 label）

## 6. Web 端（ai-agent-client）

- [x] 6.1 新增 `utils/dictCache.js`：封装 `getDict(type)`、`getDictLabel(type, code)`、`getDictOptions(type)` 方法，内存缓存
- [x] 6.2 修改 `stores/purchaseRequest.js`：删除 `UNIT_MAP`、`getUnitLabel()`、`getUnitOptions()`，改为使用 `dictCache`
- [x] 6.3 修改 `views/warehouse/MaterialListPage.vue`：删除 `unitOptions` 硬编码数组，改为从 `dictCache.getDictOptions('UNIT')` 获取

## 7. 小程序端（ai-agent-miniapp）

- [x] 7.1 新增 `utils/dictCache.js`：封装 `getDict`、`getDictLabel`、`getDictOptions` 方法，内存缓存
- [x] 7.2 修改 `pages/material/form.js`：删除 `UNITS` 数组，改为从 `dictCache` 获取单位选项
- [x] 7.3 修改 `pages/material/list.js`：删除 `UNITS` 数组和 `getUnitLabel()`，改为使用 `dictCache`
- [x] 7.4 修改 `pages/purchase/form.js`：删除内联 `unitLabels` 数组，改为使用 `dictCache`
- [x] 7.5 修改 `pages/purchase/detail.js`：删除 `UNIT_MAP` 对象，改为使用 `dictCache`
- [x] 7.6 修改 `pages/purchase/list.js`：删除 `UNIT_MAP` 对象，改为使用 `dictCache`

## 8. 验证

- [x] 8.1 后端编译通过（`./mvnw compile`）
- [ ] 8.2 Web 端单位下拉选项正常加载，物料创建/采购申请流程单位展示正确
- [ ] 8.3 小程序端单位下拉选项正常加载，物料、采购申请、采购订单单位展示正确
