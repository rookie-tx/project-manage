## Why

当前物料管理（物料库）与采购申请完全割裂——采购申请中物料名称是自由文本输入，没有从物料库选择，导致物料规格、单位每次都要手动填写，数据不统一且容易出错。同时物料管理自身 spec（规格）和 unit（单位）字段缺少必填约束，数据质量无法保证。需要将两者打通，让采购申请从物料库拉取物料并自动带出规格和单位。

## What Changes

- **BREAKING**: `t_material.unit` 从 `VARCHAR(20)` 改为 `SMALLINT NOT NULL`，使用 Unit 枚举码（0-9）
- **BREAKING**: `t_material.spec` 改为 `NOT NULL`
- **BREAKING**: `t_purchase_request` 新增 `material_id BIGINT NOT NULL` 外键关联 `t_material(id)`
- **BREAKING**: 采购申请创建/更新 API 新增 `materialId` 必填字段，后端根据物料自动填入 `name`/`specification`/`unit`
- 物料管理表单：spec 和 unit 改为必填，unit 从文本输入改为枚举下拉选择
- 采购申请表单（Web + 小程序）：物料名称从自由文本改为物料库下拉/弹窗选择，规格和单位自动带出且不可编辑
- 入库操作（Web + 小程序）：物料字段从采购申请自动带入，只读展示，不再手动选择
- 小程序新增物料管理页面（列表 + 新建/编辑表单）

## Capabilities

### New Capabilities
- `miniapp-material-management`: 小程序物料管理页面，支持物料列表查看、新建、编辑、删除

### Modified Capabilities
- `purchase-request-api`: 新增 `materialId` 必填字段，规格和单位由后端根据物料自动填充
- `purchase-request-ui`: 物料名称改为物料库下拉选择，规格和单位变为只读
- `miniapp-purchase-management`: 物料改为物料库 picker 选择，规格和单位变为只读
- `purchase-inbound`: 入库时物料从采购申请自动带入，只读展示

## Impact

- **数据库**: `t_material` 两列类型变更，`t_purchase_request` 新增一列 + 外键
- **后端-物料模块**: Domain/PO/Mapper/DTO 适配 unit 枚举
- **后端-采购申请模块**: 实体/DTO/响应新增 materialId，应用服务增加物料查询逻辑
- **Web 前端**: MaterialListPage、PurchaseRequestForm、PurchaseOrderListPage 三处改动
- **小程序**: 采购表单、入库页面改动，新增物料管理 list + form 页面
- **权限**: 物料管理权限（`material:*`）已存在，无需新增
