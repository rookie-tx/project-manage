## Context

当前 `t_material`（物料表）与 `t_purchase_request`（采购申请表）之间没有任何关联。采购申请中物料名称是自由文本输入，用户每次都要手动填写物料名、规格、单位，无法复用物料库中已维护的数据。同时物料表自身 spec 和 unit 字段缺少 NOT NULL 约束，unit 为自由文本而非枚举，数据规范性不足。

本次改动将两者打通：采购申请通过 `material_id` 外键关联物料库，选择物料后自动带出规格和单位。

## Goals / Non-Goals

**Goals:**
- 物料管理 spec 和 unit 改为必填，unit 统一使用 Unit 枚举（SMALLINT 码值）
- 采购申请创建/编辑时从物料库下拉选择物料，自动填入 name/spec/unit
- 采购申请表单中规格和单位变为只读（不可手动编辑）
- 入库操作中物料从采购申请自动带入，用户不再手动选择
- 小程序新增物料管理页面（列表 + 表单）

**Non-Goals:**
- 不涉及采购订单（t_purchase_order）表结构变更
- 不涉及审批流程变更
- 不处理存量数据迁移（本次为全新部署）
- 不涉及物料库存预警或库存管理逻辑变更

## Decisions

### 1. unit 字段类型统一为 SMALLINT 枚举码

**选择**：将 `t_material.unit` 从 `VARCHAR(20)` 改为 `SMALLINT NOT NULL`，使用 `Unit` 枚举码（0-9）。

**备选方案**：保留 VARCHAR + 映射层转换。未采用原因：两个类型永远不一致，每次都要做文本↔码值映射，维护成本高，且无法保证物料表的 unit 值一定在枚举范围内。

**影响**：
- `Material.java` 的 unit 从 `String` 改为 `Unit` 枚举
- `MaterialPO.java` 的 unit 从 `String` 改为 `Integer`
- `CreateMaterialRequest.unit` 从 `String` 改为 `@NotNull Integer`
- 前端 unit 从文本输入框改为 Select 下拉

### 2. material_id 外键 + name 冗余

**选择**：`t_purchase_request` 新增 `material_id BIGINT NOT NULL` 外键关联 `t_material(id)`，同时保留 `name` 字段作为冗余。

**备选方案**：直接替换 name 为 material_id 不保留冗余。未采用原因：列表查询每次 JOIN t_material 增加复杂度；历史数据兼容性差。

**数据流**：创建 PR 时前端传 `materialId` → 后端查 t_material → 自动填入 `name`/`specification`/`unit` → 写入 t_purchase_request。

### 3. 规格和单位由后端自动填充（非前端传值）

**选择**：前端只传 `materialId`，后端根据 materialId 查询物料并填入 name/specification/unit。

**备选方案**：前端选择物料后自行填入 spec/unit 并随请求发送。未采用原因：前端可被绕过，后端填充保证数据一致性。

**API 变更**：`CreatePurchaseRequestRequest` 和 `UpdatePurchaseRequestRequest` 中移除 `name`/`specification`/`unit` 字段（改为后端自动填充），新增 `materialId` 必填字段。

### 4. 前端规格/单位只读展示

**选择**：规格和单位字段在前端以只读文本或禁用的 input 展示，用户不可编辑。

**Web 实现**：使用 `<Input disabled>` 或纯文本 + 灰色背景展示规格和单位。

**小程序实现**：使用 `<text>` 组件展示，配合只读样式。

### 5. 入库物料从采购申请带入

**选择**：入库弹窗/页面中物料字段根据关联 PR 的 materialId 自动填充并只读。

**影响**：
- Web 端 `PurchaseOrderListPage.vue` 入库 Modal：去掉物料 Select，改为只读文本
- 小程序 `inbound.js/wxml`：物料 picker 改为只读文本展示
- 后端 `POST /api/stock-records` 保持不变（仍接收 materialId），只是前端不再让用户选择

## Risks / Trade-offs

- **[风险] 存量数据中 unit 值可能不在枚举范围** → 本次为全新部署，无存量数据，无影响
- **[风险] `materialId` NOT NULL 导致旧 PR 数据无法兼容** → 本次全新部署，旧数据无需兼容
- **[风险] 物料被删除后关联的 PR 展示异常** → t_material 使用软删除，且外键约束阻止删除已被引用的物料
- **[权衡] name 字段冗余导致数据不一致可能** → 如果物料名称后续被修改，已有 PR 的 name 不会同步更新。这是可接受的——采购申请记录的是"当时"的物料名称，属于业务快照

## Open Questions

暂无。所有关键设计决策已在探索阶段达成一致。
