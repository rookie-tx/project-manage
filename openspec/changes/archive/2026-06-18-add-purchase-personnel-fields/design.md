## Context

采购单（`t_purchase_order`）当前仅有 `created_by` 字段记录创建人，缺少对采购执行人和入库操作人的独立记录。现有系统行为：

- `createdBy` 在审批通过自动创建 PO 时从 PR 的 `createdBy` 复制，手动创建时设为 `null`
- API 响应已包含 `createdByName`（通过 `UserRepository.findById()` 解析），但 Web 端表格未展示
- 小程序详情页将 `createdByName` 标签显示为"采购员"，语义不准确
- `t_stock_record` 有 `operator_id` 记录入库操作人，但未回写到 PO 表，也未解析用户名返回前端

目标是在采购单上增加两个独立的人员字段，在关键操作节点自动记录当前用户，并在前后端统一展示用户名。

## Goals / Non-Goals

**Goals:**
- 在 `t_purchase_order` 新增 `purchaser_id` 和 `inbound_operator_id` 列
- 采购完成时（PURCHASING→PURCHASED）自动将 `LoginContextHolder.getUserId()` 写入 `purchaser_id`
- 入库时（PURCHASED→ARRIVED）自动将 `LoginContextHolder.getUserId()` 写入 `inbound_operator_id`
- API 响应返回 `purchaserName` 和 `inboundOperatorName`（通过 `UserRepository.findById()` 解析）
- Web 端表格和详情展示采购人、入库人
- 小程序列表和详情展示采购人、入库人

**Non-Goals:**
- 不提供前端手动选择采购人/入库人的下拉框（自动记录当前用户即可）
- 不修改 `t_stock_record` 的 `operator_id` 逻辑（保持现状）
- 不改变现有的权限模型
- 不修改 `createdBy` 的现有逻辑

## Decisions

### 决策 1: 独立字段 vs 复用 createdBy

**选择**: 新增独立的 `purchaser_id` 和 `inbound_operator_id` 字段

**理由**: `createdBy` 记录的是采购单的创建者，而采购人是实际执行采购操作的人，两者可能不同。例如：张三提交了采购申请，审批通过后系统自动生成 PO（createdBy=张三），但实际由李四去联系供应商完成采购。独立字段语义清晰，便于追溯。

### 决策 2: PO 表新增字段 vs 从 stock_record 关联查询

**选择**: 在 `t_purchase_order` 表直接新增 `inbound_operator_id`

**理由**: 查询简单、性能好（无需 JOIN `t_stock_record`），数据冗余可控（一个 PO 只记录一次入库人）。即使未来一张 PO 对应多次入库，记录最后一次操作人也有业务意义。

### 决策 3: 赋值时机

**选择**: 采购人在 `changeStatus(PURCHASING→PURCHASED)` 时赋值，入库人在 `MaterialController.recordStock()` 中赋值

**理由**: 这两个节点是用户明确执行"采购完成"和"入库"操作的时刻，此时 `LoginContextHolder` 中已有当前用户。采购人设在状态变更而非编辑保存时，因为一个人可能编辑供应商信息后由另一个人确认采购完成。

### 决策 4: 用户名解析方式

**选择**: 在 `toResponse()` 中通过 `UserRepository.findById()` 逐个解析，与现有 `createdByName` 模式一致

**理由**: 保持代码风格一致。如果后续出现性能瓶颈（列表大量查询），可改为批量查询或 SQL JOIN，但当前数据量下逐个查询即可。

### 决策 5: 入库人写入位置

**选择**: 在 `MaterialController.recordStock()` 中直接调用 `purchaseOrderRepository.updateInboundOperator()` 写入

**理由**: `PURCHASED→ARRIVED` 的状态变更实际发生在 `MaterialController` 而非 `PurchaseOrderApplicationService.changeStatus()`。保持这个调用路径，在状态更新的同时写入入库人。不新增独立的 API 端点。

## Risks / Trade-offs

- **风险**: `UserRepository.findById()` 返回空（用户被删除）→ **缓解**: `toResponse()` 已有 null 检查模式，找不到用户时字段为 null，前端用 `v-if` 条件渲染
- **风险**: 入库操作在 `MaterialController` 而非 `PurchaseOrderApplicationService`，调用链跨模块 → **缓解**: 复用已有的 `purchaseOrderRepository` 依赖（`MaterialController` 已注入），不引入新的模块耦合
- **风险**: 旧数据没有 `purchaser_id` 和 `inbound_operator_id` → **缓解**: 新增列允许 NULL，旧数据显示为空，不做回填
