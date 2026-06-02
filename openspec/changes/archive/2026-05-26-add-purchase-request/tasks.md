## 1. 后端：项目下拉选择接口

- [x] 1.1 ProjectMapper.xml 新增 selectAllActive 查询（仅查 id, code, name，过滤 is_deleted=0）
- [x] 1.2 ProjectMapper 接口新增 `List<ProjectPO> selectAllActive()` 方法
- [x] 1.3 ProjectRepository 接口新增 `List<Project> findAllActive()` 方法
- [x] 1.4 ProjectRepositoryImpl 实现 findAllActive
- [x] 1.5 新建 ProjectSelectController，GET /api/projects/select，调用 ProjectRepository.findAllActive
- [x] 1.6 新建 ProjectSelectResponse DTO（id + code + name）

## 2. 后端：采购申请 — domain 层

- [x] 2.1 新建 PurchaseRequestStatus 枚举（PENDING/CANCELLED），含 code 和 label
- [x] 2.2 新建 Unit 枚举（10 个值），含 code 和 label
- [x] 2.3 新建 PurchaseRequest 领域实体（@Getter、私有构造、fromPersistence 工厂、create 工厂、update/transitionTo/markDeleted 方法）
- [x] 2.4 新建 PurchaseRequestRepository 接口（findById/findByPage/save/deleteById）
- [x] 2.5 新建 PurchaseRequestDomainService（create/update/softDelete，构造器注入）

## 3. 后端：采购申请 — infrastructure 层

- [x] 3.1 新建 PurchaseRequestPO
- [x] 3.2 新建 PurchaseRequestMapper 接口 + PurchaseRequestMapper.xml（insert/selectById/selectByPage/update/deleteById）
- [x] 3.3 新建 PurchaseRequestPoMapper（MapStruct，PO↔Domain，BigDecimal↔Double 转换）
- [x] 3.4 新建 PurchaseRequestRepositoryImpl
- [x] 3.5 新建 PurchaseRequestDomainConfig（@Configuration 暴露 DomainService Bean）
- [x] 3.6 schema.sql 新增 t_purchase_request 建表语句

## 4. 后端：采购申请 — application 层

- [x] 4.1 新建 PurchaseRequestDtoMapper（MapStruct，Domain↔Request/Response）
- [x] 4.2 新建 PurchaseRequestApplicationService（create/list/detail/update/delete，处理异常转换，联查项目名和用户名）

## 5. 后端：采购申请 — api 层

- [x] 5.1 新建 CreatePurchaseRequestRequest DTO（含 @NotBlank/@NotNull 校验）
- [x] 5.2 新建 UpdatePurchaseRequestRequest DTO
- [x] 5.3 新建 PurchaseRequestResponse DTO
- [x] 5.4 新建 PurchaseRequestListResponse DTO（items + total + page + size）
- [x] 5.5 新建 PurchaseRequestController（POST/GET/PUT/DELETE 5 个端点）
- [x] 5.6 ErrorCode 枚举新增 4xxx 范围（PROJECT_NOT_FOUND/PURCHASE_REQUEST_NOT_FOUND/STATUS_TRANSITION_INVALID）

## 6. 前端：Store

- [x] 6.1 新建 stores/purchaseRequest.js — usePurchaseRequestStore（list/create/detail/update/remove，含 Unit 枚举映射、状态映射、状态流转表）

## 7. 前端：共享组件

- [x] 7.1 新建 components/ProjectSelect.vue — 封装 a-select，onMounted 调 GET /api/projects/select，v-model:value 绑定 projectId
- [x] 7.2 新建 components/PurchaseRequestForm.vue — 表单复用组件，支持 mode="create"/"edit"，字段：项目(ProjectSelect)、名称、规格、品牌、数量、单位(Select)、到场时间(DatePicker)、备注(Textarea)；编辑模式额外显示状态(Select，仅可选合法流转目标)

## 8. 前端：页面

- [x] 8.1 新建 views/purchase/PurchaseRequestListPage.vue — a-table + 项目筛选下拉 + 分页 + 操作按钮（查看/编辑/删除），与 URL query 同步
- [x] 8.2 新建 views/purchase/PurchaseRequestCreatePage.vue — 使用 PurchaseRequestForm（mode="create"），成功跳转 /procurement
- [x] 8.3 新建 views/purchase/PurchaseRequestDetailPage.vue — a-descriptions 只读展示 + 编辑/删除/返回按钮
- [x] 8.4 新建 views/purchase/PurchaseRequestEditPage.vue — 使用 PurchaseRequestForm（mode="edit"），成功/取消调用 router.back()

## 9. 前端：路由与导航

- [x] 9.1 router/index.js 新增 4 条路由：/procurement、/procurement/new、/procurement/:id、/procurement/:id/edit
- [x] 9.2 DashboardLayout.vue 顶栏菜单新增"采购申请"菜单项
