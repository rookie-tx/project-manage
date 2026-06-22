## Why

项目管理者需要直观查看每个项目下各类物料的采购汇总数据（采购了哪些物料、各多少数量、花了多少钱），目前缺乏这个统计视图。同时 web 端和小程序端都缺乏通用的打印能力，用户无法将统计报表输出为纸质文档。

## What Changes

- 项目管理列表页操作列新增「统计」按钮，进入该项目的物料采购统计页面
- 统计页面按物料维度汇总已完成的采购数据：物料名称、规格、单位、总采购数量、总采购金额
- 后端新增 `GET /api/projects/{id}/purchase-statistics` 接口，JOIN 采购申请、采购单、物料表，按项目过滤已到货的采购单，按物料分组聚合
- Web 端新增通用 `usePrint()` 组合式函数，打开新窗口渲染 A4 打印模板并自动调用浏览器打印
- 小程序端新增 Canvas 绘制统计表格 → 生成图片 → 用户保存打印
- 小程序端项目列表新增统计入口

## Capabilities

### New Capabilities
- `project-purchase-stats`: 项目管理下的物料采购统计，展示指定项目各物料的采购数量与金额汇总
- `print-service`: 通用打印服务，Web 端基于新窗口+CSS打印，小程序端基于 Canvas 图片生成

### Modified Capabilities
<!-- None -->

## Impact

- **后端**: ProjectController 新增统计接口；新建统计 Mapper 查询
- **Web 前端**: 新增 ProjectPurchaseStatsPage 页面和路由；新增 usePrint composable；ProjectListPage 操作列新增按钮
- **小程序**: 新增项目统计页面；Canvas 打印能力；项目列表页操作列新增入口
