## Context

项目当前已有仓库管理模块（出入库流水 `StockRecord`），其"记录运出"的模式与建筑废渣运出类似。但项目目前没有任何文件上传能力——零引用 `MultipartFile`，零文件/附件表，前端 Axios 客户端硬编码 `Content-Type: application/json`。

本设计需要同时建设通用文件上传基础设施和建筑废渣业务模块，遵循项目既有的 DDD-lite 四层架构和 Vue 3 + Ant Design Vue 前端模式。

## Goals / Non-Goals

**Goals:**
- 提供通用文件上传能力（本地磁盘存储），后续其他模块可复用
- 提供建筑废渣运出记录的完整 CRUD（创建、列表查询、详情查看、软删除）
- 创建运出记录时必须上传至少一张照片
- 记录运输车辆（车牌号、司机姓名）和处置去向
- 列表支持按项目、日期范围、废渣种类筛选
- 全部遵循项目现有分层架构、命名约定和代码模式

**Non-Goals:**
- 不涉及审批流程（运出记录直接创建，不走 Flowable）
- 不涉及编辑/修改已创建的运出记录（与 StockRecord 一致，创建后不可变）
- 不做图片处理（缩放、水印、压缩等）
- 不使用云存储（OSS/S3）——但通用文件模块的接口设计不排斥未来切换
- 不修改现有模块的代码或数据库表

## Decisions

### 1. 文件存储：本地磁盘 + 可配置路径

**选择**：文件存储到服务器本地磁盘，上传根目录通过 `application.yml` 中的 `app.upload.dir` 配置项指定，默认 `/data/uploads`。

**文件命名**：`{UUID}.{ext}` —— 使用 UUID 避免文件名碰撞，保留原始扩展名用于 MIME 推断。原始文件名存入 `t_upload_file.original_name`。

**目录结构**：`{uploadDir}/{business_type}/{yyyy-MM}/{uuid}.{ext}` —— 按业务类型和年月分目录，避免单目录文件过多。

**备选方案**：MinIO / 阿里云 OSS —— 功能更完善但增加运维复杂度。当前阶段本地磁盘足够。由于通用文件模块的 `stored_path` 已经是相对路径，后续切换存储后端只需改 `FileController` 的实现。

### 2. 上传流程：先上传后提交

**流程**：用户选择照片 → 前端逐张上传到 `/api/files/upload` → 获得 `fileId` 列表 → 提交表单 JSON（含 `fileIds`）

**优点**：
- 表单提交仍为 `application/json`，不破坏现有 API 客户端模式
- 上传失败可以即时反馈，不必等表单提交
- 照片与废渣记录的关联在业务层完成（创建记录时更新 `t_upload_file.business_type` / `business_id`）

**孤儿文件处理**：如果用户上传了照片但未完成表单提交，文件将保留在磁盘但 `business_type` 和 `business_id` 为 NULL。这是可接受的权衡（磁盘空间充裕，孤儿文件可通过定时任务清理）。

### 3. 通用文件模块 vs 业务内嵌

**选择**：将文件上传设计为独立通用模块 `cn.tx.aiagent.file`，通过 `business_type` + `business_id` 与业务记录关联。

| 维度 | 通用模块 | 业务内嵌 |
|------|---------|---------|
| 复用性 | ✅ 后续模块直接复用 | ❌ 每次重复建设 |
| 耦合度 | 低，通过 business_type 解耦 | 高 |
| 复杂度 | 多一层抽象 | 简单直接 |
| 当前成本 | 略高 | 低 |

项目目前已有 8+ 业务模块，未来很可能有更多上传需求（采购附件、项目文档等）。通用模块的前期投资是合理的。

### 4. 记录不可编辑

与 `StockRecord`（出入库流水）保持一致：记录一旦创建不可修改。如需更正，操作人应删除旧记录后重新创建。这简化了设计并保证了数据可追溯性。

### 5. 废渣种类和单位：自由文本

不做枚举或字典表，使用 `VARCHAR` 自由输入。理由：
- 工地废渣种类多变，枚举难以穷举
- 避免额外的字典维护界面
- 前端可以使用带建议的 AutoComplete 组件改善体验（可选优化）

### 6. 数据库表设计

`t_upload_file` 的设计要点：
- `business_type` + `business_id` 组合用于关联业务记录，但可为 NULL（上传时尚未关联）
- 不使用软删除（文件物理删除由业务记录删除时触发）
- 无 `updated_at`（文件不可修改，要么存在要么被删除）

`t_construction_waste` 的设计要点：
- 遵循项目建表规范：`BIGSERIAL` 主键、`SMALLINT` 状态/标记、`created_at` + `updated_at`、`updated_at` 触发器
- `is_deleted` 软删除标记
- `operator_id` 记录操作人，通过 Session 获取当前登录用户

## Risks / Trade-offs

- **[孤儿文件] 用户上传照片后放弃提交表单** → 文件占用磁盘但不影响业务。后续可通过定时任务清理 `business_type IS NULL AND created_at < now() - interval '1 day'` 的文件
- **[本地存储单点故障]** → 部署时确保上传目录有定期备份；后续可平滑迁移到共享存储/OSS
- **[大文件上传]** → 当前无大小限制，Spring Boot 默认 `spring.servlet.multipart.max-file-size` 为 1MB，需在 application.yml 中显式配置为合理值（如 10MB）
- **[并发安全]** → 废渣记录创建时通过业务层更新 `t_upload_file` 的 `business_id`，在事务内完成，保证一致性
