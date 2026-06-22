## Context

当前系统有完整的采购流程（采购申请→采购单→报价→入库），但缺少按项目维度汇总物料采购情况的能力。同时 web 端和小程序端都没有打印功能，用户需要将统计数据输出为纸质文档。

项目详情页已经有 3 个 tab（基本信息、临时工、打卡区域），统计入口不走详情页 tab，而是在项目列表页操作列增加按钮，进入独立统计页面。

## Goals / Non-Goals

**Goals:**
- 提供按项目+物料维度的采购统计（仅统计已到货的采购单）
- Web 端提供可复用的通用打印能力（新窗口 + A4 CSS 打印）
- 小程序端提供 Canvas 生成打印图片的能力
- 统计页面对两端均可用

**Non-Goals:**
- 不涉及导出 Excel/PDF（本期只做打印）
- 不修改现有采购流程
- 不在项目详情页增加统计 tab（独立页面路由）
- 不在统计页中做图表可视化（本期只做表格）

## Decisions

### 1. 统计入口位置

**决定**: 项目列表页操作列增加「统计」按钮，跳转到独立统计页面 `/projects/:id/stats`。

**理由**: 独立页面路由更清晰，后续可扩展；不与详情页 tab 耦合。

### 2. 后端 API 设计

**决定**: 在 `ProjectController` 中新增 `GET /api/projects/{id}/purchase-statistics` 接口，返回 `List<MaterialPurchaseStatsResponse>`。

**理由**: 统计属于项目维度的查询，放在 ProjectController 语义合理，避免创建新的 Controller。

### 3. 统计口径

**决定**: 统计已到货（`po.status = 2`）的采购单，GROUP BY 物料。

**理由**: 用户明确"已完成采购"指已到货。审批中或采购中的未完成数据不纳入统计。

### 4. Web 端打印方案

**决定**: 创建 `usePrint()` 组合式函数，通过 `window.open()` 打开新窗口，渲染预定义的 A4 打印模板（含 `@media print` CSS），延迟 300ms 后自动调用 `window.print()`，打印完成后自动关闭。

**备选方案**: `print-js` 库 → 拒绝，增加外部依赖但功能用不上；`window.print()` 直接打当前页 → 拒绝，无法控制打印内容和排版。

### 5. 小程序端打印方案

**决定**: Canvas 绘制表格 → `canvasToTempFilePath()` 生成 PNG → 预览页面 → 用户长按保存或分享打印。

**理由**: 小程序无 `window.print()`，Canvas 方案不依赖第三方服务，且统计表格式规整容易绘制。

### 6. 数据查询方式

**决定**: 单条 SQL 完成 JOIN + GROUP BY，不引入 Java 层聚合。

**理由**: 按项目过滤后的物料维度聚合数据量小（一个项目通常 < 50 种物料），单条 SQL 即可完成，无需引入内存聚合或 Redis 缓存。

```sql
SELECT
    m.id, m.name, m.spec, m.unit,
    SUM(pr.quantity) AS total_quantity,
    SUM(po.total_amount) AS total_amount
FROM t_purchase_request pr
JOIN t_purchase_order po ON pr.id = po.purchase_request_id
JOIN t_material m ON pr.material_id = m.id
WHERE pr.project_id = #{projectId}
  AND po.status = 2
  AND pr.is_deleted = 0
  AND po.is_deleted = 0
GROUP BY m.id, m.name, m.spec, m.unit
ORDER BY total_amount DESC
```

## Risks / Trade-offs

- **LEFT JOIN vs INNER JOIN**: 用 INNER JOIN，只统计有关联采购单且已到货的数据。如果采购申请没有对应的采购单（异常数据），不会出现在统计中，这是合理的行为。
- **大项目数据量**: 单个项目的物料种类有限，聚合查询性能可控。如果未来需要跨项目统计，可能需要引入缓存。
- **Canvas 绘制兼容性**: 小程序 Canvas 在不同机型上表现可能有差异，统计列数固定（5列），布局简单，风险可控。
- **打印窗口被拦截**: 浏览器可能拦截 `window.open()`。usePrint 应该在用户点击事件的同步代码中调用 `window.open()`（而非异步回调中），以避免弹窗拦截。

## Open Questions

<!-- 无 -->
