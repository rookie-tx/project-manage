## 1. 数据库变更

- [x] 1.1 在 `schema.sql` 中新增 `t_upload_file` 建表语句（id, business_type, business_id, original_name, stored_path, file_size, content_type, created_at）
- [x] 1.2 在 `schema.sql` 中新增 `t_construction_waste` 建表语句（id, project_id, waste_type, quantity, unit, vehicle_plate, driver_name, destination, removal_date, remark, operator_id, is_deleted, created_at, updated_at）及 `updated_at` 触发器
- [x] 1.3 在 `schema.sql` 末尾新增两张表的 `setval` 序列校准语句
- [x] 1.4 在 `schema.sql` 种子数据中新增 `waste:read`、`waste:create`、`waste:delete` 权限，并分配给 admin 角色

## 2. 后端 — 通用文件模块 (cn.tx.aiagent.file)

- [x] 2.1 创建 `application.yml` 文件上传配置项（`spring.servlet.multipart.max-file-size`、`app.upload.dir`）
- [x] 2.2 创建 `domain/UploadFile.java` — 领域实体（id, businessType, businessId, originalName, storedPath, fileSize, contentType, createdAt），静态工厂方法 `fromPersistence`
- [x] 2.3 创建 `domain/UploadFileRepository.java` — 仓储接口（save, findById, findByBusiness, delete, updateBusinessId）
- [x] 2.4 创建 `infrastructure/UploadFilePO.java` — 持久化对象
- [x] 2.5 创建 `infrastructure/UploadFilePoMapper.java` — MapStruct PO↔Domain 映射器
- [x] 2.6 创建 `infrastructure/UploadFileMapper.java` — MyBatis Mapper 接口
- [x] 2.7 创建 `infrastructure/UploadFileMapper.xml` — MyBatis SQL 映射文件
- [x] 2.8 创建 `infrastructure/UploadFileRepositoryImpl.java` — 仓储实现
- [x] 2.9 创建 `infrastructure/FileDomainConfig.java` — 暴露领域服务/仓储 Bean
- [x] 2.10 创建 `FileStorageProperties.java` — `@ConfigurationProperties` 读取上传目录配置
- [x] 2.11 创建 `api/FileController.java` — 文件上传（POST multipart）、下载/预览（GET）、删除（DELETE）三个端点
- [x] 2.12 在 `common/api/ErrorCode.java` 中新增文件相关错误码（FILE_TOO_LARGE, FILE_TYPE_NOT_ALLOWED, FILE_NOT_FOUND）
- [x] 2.13 在 `i18n/messages.properties` 和 `messages_en_US.properties` 中新增文件相关错误消息

## 3. 后端 — 建筑废渣模块 (cn.tx.aiagent.constructionwaste)

- [x] 3.1 创建 `domain/ConstructionWaste.java` — 领域实体（id, projectId, wasteType, quantity, unit, vehiclePlate, driverName, destination, removalDate, remark, operatorId, isDeleted, createdAt, updatedAt），静态工厂方法 `create` 和 `fromPersistence`
- [x] 3.2 创建 `domain/ConstructionWasteRepository.java` — 仓储接口（save, findById, findAll, search, softDelete）
- [x] 3.3 创建 `infrastructure/ConstructionWastePO.java` — 持久化对象
- [x] 3.4 创建 `infrastructure/ConstructionWastePoMapper.java` — MapStruct PO↔Domain 映射器
- [x] 3.5 创建 `infrastructure/ConstructionWasteMapper.java` — MyBatis Mapper 接口（含搜索查询方法）
- [x] 3.6 创建 `infrastructure/ConstructionWasteMapper.xml` — MyBatis SQL 映射文件（含动态筛选查询）
- [x] 3.7 创建 `infrastructure/ConstructionWasteRepositoryImpl.java` — 仓储实现（创建时在事务中回填关联文件 business_id）
- [x] 3.8 创建 `infrastructure/ConstructionWasteDomainConfig.java` — 暴露领域服务/仓储 Bean
- [x] 3.9 创建 `api/CreateConstructionWasteRequest.java` — 创建请求 DTO（含 @NotNull/@NotBlank 校验，fileIds 至少 1 个）
- [x] 3.10 创建 `api/ConstructionWasteResponse.java` — 响应 DTO（含关联文件列表）
- [x] 3.11 创建 `api/ConstructionWasteController.java` — 列表查询（GET，含 projectId/startDate/endDate/wasteType 筛选）、详情（GET）、创建（POST）、软删除（DELETE）
- [x] 3.12 在 `common/api/ErrorCode.java` 中新增废渣相关错误码（WASTE_PHOTO_REQUIRED, WASTE_NOT_FOUND）
- [x] 3.13 在 `i18n/messages.properties` 和 `messages_en_US.properties` 中新增废渣相关错误消息

## 4. 前端 — API 与 Store

- [x] 4.1 创建 `src/api/fileUpload.js` — 文件上传专用函数（使用 FormData + axios，不走通用 client 拦截器，单独设置 multipart header）
- [x] 4.2 创建 `src/stores/constructionWaste.js` — Pinia setup store（records, loading, error, fetchAll, fetchById, create, remove）
- [x] 4.3 在 `src/api/client.js` 或新建 `src/api/constructionWaste.js` 中添加废渣 API 封装函数

## 5. 前端 — 页面组件

- [x] 5.1 创建 `src/views/waste/ConstructionWasteListPage.vue` — 列表页：a-table 展示数据，筛选栏（项目 Select + 日期范围 DatePicker + 种类 Input），新建按钮，删除按钮
- [x] 5.2 创建 `src/views/waste/ConstructionWasteCreatePage.vue` — 新建页：a-form 表单（项目选择、种类、数量、单位、车牌号、司机、去向、日期、备注），a-upload 照片上传（必传校验），提交逻辑（先逐张上传照片获取 fileIds，再提交表单）
- [x] 5.3 创建 `src/views/waste/ConstructionWasteDetailPage.vue` — 详情页：只读展示所有字段，照片网格展示（使用 a-image 预览）

## 6. 前端 — 路由与导航

- [x] 6.1 在 `src/router/index.js` 中新增 3 条路由：`/construction-wastes`、`/construction-wastes/new`、`/construction-wastes/:id`，meta 配置 `requiresAuth: true` 和对应权限码
- [x] 6.2 在侧边栏菜单组件中新增"建筑废渣"菜单项（需定位现有侧边栏组件）

## 7. 验证

- [x] 7.1 启动后端服务，验证数据库迁移正确（两张新表存在、权限种子数据正确）
- [x] 7.2 验证文件上传流程：上传图片 → 检查磁盘文件存在 → 检查数据库记录 → 下载预览
- [x] 7.3 验证废渣记录创建：上传照片 → 创建记录 → 检查照片已关联 → 查看详情
- [x] 7.4 验证列表筛选：按项目、日期范围、废渣种类分别筛选
- [x] 7.5 验证软删除：删除记录 → 记录标记为已删除、关联文件物理删除
- [x] 7.6 验证权限控制：无权限用户访问接口返回 403/ACCESS_DENIED
