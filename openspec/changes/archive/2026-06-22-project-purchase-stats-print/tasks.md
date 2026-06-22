## 1. 后端 — 统计接口

- [x] 1.1 新增 `MaterialPurchaseStatsResult` 查询结果对象，用于 MyBatis 映射统计 SQL 结果
- [x] 1.2 新建 `MaterialPurchaseStatsMapper.xml` 或在现有 ProjectMapper.xml 中添加统计 SQL（JOIN t_purchase_request + t_purchase_order + t_material，按 project_id 过滤、po.status=2，GROUP BY material）
- [x] 1.3 新增 `MaterialPurchaseStatsResponse` DTO（字段：materialId, materialName, materialSpec, materialUnit, totalQuantity, totalAmount）
- [x] 1.4 在 `ProjectController` 中新增 `GET /api/projects/{id}/purchase-statistics` 接口，调用统计查询并返回 Response 列表

## 2. Web 前端 — 通用打印服务

- [x] 2.1 新建 `src/composables/usePrint.js` 组合式函数，包含 `openPrint({ title, columns, data })` 方法，打开新窗口渲染 A4 打印模板
- [x] 2.2 打印模板包含 `@media print` CSS（A4 尺寸、页边距、表格边框），渲染完成后自动调用 `window.print()`，打印对话框关闭后自动关闭窗口

## 3. Web 前端 — 物料采购统计页面

- [x] 3.1 新建 `src/views/project/ProjectPurchaseStatsPage.vue`，展示项目名称 + 物料采购统计表格（物料名称、规格、单位、总采购数量、总采购金额）+ 打印按钮
- [x] 3.2 新增路由 `/projects/:id/stats`，指向 ProjectPurchaseStatsPage
- [x] 3.3 修改 `ProjectListPage.vue`，操作列新增「统计」按钮，跳转到 `/projects/:id/stats`

## 4. 小程序 — 物料采购统计

- [x] 4.1 新建 `pages/project/stats` 页面（js/wxml/wxss/json），调用统计 API 并展示物料采购统计列表
- [x] 4.2 小程序项目列表页操作列新增「统计」入口
- [x] 4.3 使用 Canvas 2D API 绘制统计表格，通过 `canvasToTempFilePath` 生成 PNG 图片，支持预览和保存打印
