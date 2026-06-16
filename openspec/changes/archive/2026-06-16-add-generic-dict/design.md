## Context

当前项目中计量单位通过 `Unit.java` 硬编码枚举管理，枚举定义在 `purchaserequest` 模块但被 `warehouse` 模块跨包引用。新增单位需要修改代码并重新部署。前端（Web 端和小程序端）各自硬编码了单位映射，共重复 8 处。

## Goals / Non-Goals

**Goals:**
- 将单位（Unit）从硬编码枚举迁移到数据库字典表，新增单位只需 INSERT 数据
- 通用字典表设计，type 字段区分字典类型，为未来其他常量（如项目类型）预留扩展空间
- 前后端统一通过 API 获取字典数据，消除 8 处硬编码重复
- 关联表存储字典 code（VARCHAR），而非数字 ID，无需 JOIN 即可读懂数据

**Non-Goals:**
- 不迁移其他枚举（如 PurchaseRequestStatus、ProjectStatus）到字典表
- 不做现有数据迁移（开发/测试环境直接改库）
- 不为建筑废渣的 String 类型 unit 引入字典约束（保持自由文本）
- 字典表不提供 CRUD 管理界面，仅提供查询 API（管理操作直接操作数据库）

## Decisions

### 1. 字典表结构：单表 `t_dict`，用 `type` 字段区分类型

```
t_dict
├── id       BIGINT PK AUTO_INCREMENT
├── type     VARCHAR(32)  → 字典类型，如 'UNIT'
├── code     VARCHAR(32)  → 项编码，如 'TON'
├── label    VARCHAR(64)  → 显示名，如 '吨'
├── sort     INT          → 排序权重
├── enabled  TINYINT      → 0=禁用, 1=启用
UNIQUE KEY (type, code)
```

**选择原因**：单表比多表简单，`type` 字段天然支持分组。唯一约束 `(type, code)` 保证同类型下 code 不重复。

**备选方案**：双表（`t_dict_group` + `t_dict_item`）更规范化但增加了 JOIN 复杂度，当前规模下不必要。

### 2. 关联字段存储 code 而非 id

`t_purchase_request.unit` 和 `t_material.unit` 改为存储 `VARCHAR(32)` 的字典 code（如 'TON'、'METER'），而非字典表的 id。

**选择原因**：SQL 查询结果无需 JOIN 即可理解（`WHERE unit = 'TON'`），代码中也不需要额外查表。如果存 id，`WHERE unit = 5` 就失去了可读性。

**备选方案**：存储字典表 id（用外键）更规范但增加 JOIN 成本。

### 3. 后端模块位置：`cn.tx.aiagent.dict`

新建独立 `dict` 模块，遵循四层 DDD 架构。Controller 放在 `dict/api/`，领域实体放在 `dict/domain/`，MyBatis Mapper 放在 `dict/infrastructure/`。

**选择原因**：字典是独立领域概念，独立模块便于未来扩展（如增加管理接口）。

### 4. 前端标签映射：API 获取 + 内存缓存

前端新增 `utils/dictCache.js`，提供 `getDict(type)`、`getDictLabel(type, code)`、`getDictOptions(type)` 三个方法。首次调用时从 `GET /api/dicts/{type}` 获取数据并缓存，后续调用直接返回缓存。

**选择原因**：字典数据变更频率极低（几乎不变），缓存后无需重复请求。比在 store 中管理更轻量，可跨 store 共享。

### 5. 采购申请响应中 label 的填充策略

采购申请列表/详情接口返回 `unitCode`（String 字典 code），**不再由后端填充 `unitLabel`**。前端通过 `dictCache.getLabel('UNIT', record.unitCode)` 自行获取显示文本。

**选择原因**：避免后端每个涉及单位的接口都 JOIN `t_dict` 表。前端已有缓存，一次请求即可覆盖所有展示场景。

**例外**：`materialUnit` 字段 — 采购申请的物料单位是跟随物料的，物料列表接口响应中包含 `unitCode`，前端在回填时自行查找 label。

### 6. 数据库列类型变更（无迁移）

`t_purchase_request.unit` SMALLINT → VARCHAR(32)，`t_material.unit` INTEGER → VARCHAR(32)。由于是开发/测试环境，直接 `ALTER TABLE ... MODIFY COLUMN` 改列类型，不做数据迁移脚本。

## Risks / Trade-offs

- **[数据一致性风险]** 如果字典表中某 code 被删除或禁用，关联表可能残留无效 code → **缓解**：字典表只做逻辑禁用（enabled=0），不物理删除；前端下拉框过滤 disabled 项但已存数据仍可正常显示
- **[性能影响]** 每次列表请求不再 JOIN 字典表填 label，改为前端缓存查找 → **影响**：无额外 DB 开销，前端缓存查找是 O(1) 的 Map 查找，性能更好
- **[字符串 code 比整数占用空间大]** VARCHAR(32) vs SMALLINT(2 bytes) → **影响**：对于单位这种低频数据（每行一个单位 code），存储差异可忽略

## Open Questions

无
