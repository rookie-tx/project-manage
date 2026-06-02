## Why

建筑工地每天产生大量废渣（混凝土块、砖块、土方等），运出处置时需要留存照片作为合规证据。目前系统缺少对建筑废渣的管理能力，无法追踪废渣去向、无法留存影像证据，存在管理盲区。

## What Changes

- **新增通用文件上传模块**：提供文件上传、下载/预览、删除能力，文件存储至本地磁盘。该模块为通用设计，后续采购、项目等模块可复用
- **新增建筑废渣管理模块**：支持废渣运出记录的创建、列表查询、详情查看和软删除
- **运出记录必须关联项目**：每条废渣记录归属于一个项目
- **照片必传**：创建运出记录时至少上传一张现场照片作为证据
- **记录运输信息**：包括车牌号、司机姓名、处置去向、运出日期
- **新增权限**：`waste:read`、`waste:create`、`waste:delete`

## Capabilities

### New Capabilities

- `file-upload`: 通用文件上传能力 — 支持 multipart 上传、存储到本地磁盘、下载/预览、删除。文件按 `business_type` + `business_id` 与业务记录关联，可被多个业务模块复用
- `construction-waste-management`: 建筑废渣管理能力 — 运出记录的 CRUD，包含废渣种类、数量、单位、车辆/司机信息、处置去向、现场照片。列表支持按项目、日期范围、废渣种类筛选

### Modified Capabilities

<!-- 本次不修改任何现有 spec，全部为新增 -->

## Impact

- **数据库**：新增 `t_construction_waste`（建筑废渣运出记录表）、`t_upload_file`（通用上传文件表）；`schema.sql` 新增建表语句和序列校准
- **后端**：新增 `cn.tx.aiagent.file`（通用文件模块）和 `cn.tx.aiagent.constructionwaste`（废渣业务模块）两个包，遵循 DDD-lite 四层架构
- **前端**：新增 3 个页面（列表/新建/详情），1 个 Pinia store（constructionWaste），3 条路由，侧边栏菜单项
- **权限**：新增 3 个权限码（`waste:read`、`waste:create`、`waste:delete`）及种子数据
- **API**：新增 `/api/files/*`（3 个端点）、`/api/construction-wastes/*`（4 个端点）
- **配置**：新增文件上传目录配置项
- **依赖**：无新增外部依赖，文件上传使用 Spring Boot 内置 `MultipartFile` 处理
