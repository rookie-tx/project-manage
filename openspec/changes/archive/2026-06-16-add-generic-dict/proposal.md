## Why

当前项目中计量单位通过 `Unit.java` 硬编码枚举管理，新增单位（如"千克"、"升"）需要修改代码并重新部署；同一份单位映射在后端、Web 前端、小程序中重复定义 8 次，维护成本高且容易不一致。

## What Changes

- **BREAKING**: 新增 `t_dict` 通用字典表，通过 `type` 字段区分字典类型（当前仅实现 `UNIT` 类型）
- **BREAKING**: 删除 `Unit.java` 枚举类，`PurchaseRequest.unit` 和 `Material.unit` 从枚举改为字典 code（String 类型）
- **BREAKING**: 数据库列类型变更：`t_purchase_request.unit` SMALLINT → VARCHAR(32)，`t_material.unit` INTEGER → VARCHAR(32)
- 新增 `GET /api/dicts/{type}` 接口，支持按类型获取字典项列表
- Web 端和小程序端移除硬编码的单位映射，改为从 API 获取并缓存
- 前端新增统一的 `dictCache` 工具模块，提供 `getLabel(code)` 和 `getOptions()` 方法

## Capabilities

### New Capabilities
- `dict-api`: 通用字典查询接口，`GET /api/dicts/{type}` 返回指定类型的所有启用字典项

### Modified Capabilities
- `purchase-request-api`: `PurchaseRequestResponse.unitCode` 字段类型从 Integer 改为 String，`create` 和 `update` 接口的 unit 字段从 Integer 改为 String（字典 code，如 "TON"）

## Impact

- 后端：新增 `cn.tx.aiagent.dict` 模块（Controller + Domain + Infrastructure），修改 `purchaserequest` 和 `warehouse` 模块中 14 个文件，删除 1 个文件
- 数据库：新增 `t_dict` 表并插入 10 条单位种子数据，修改 `t_purchase_request.unit` 和 `t_material.unit` 列类型
- Web 端（ai-agent-client）：修改 `stores/purchaseRequest.js` 和 `views/warehouse/MaterialListPage.vue`，新增 `utils/dictCache.js`
- 小程序端（ai-agent-miniapp）：修改 5 个页面的硬编码单位映射，新增统一的字典缓存工具
- 不做数据迁移，直接改结构（开发/测试环境）
