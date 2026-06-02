## 1. 后端：用户列表接口

- [x] 1.1 UserMapper.xml 新增 selectAllActive 查询，只查 id 和 username，过滤 status=1
- [x] 1.2 UserMapper 接口新增 `List<UserPO> selectAllActive()` 方法
- [x] 1.3 UserRepository 接口新增 `List<User> findAllActive()` 方法
- [x] 1.4 UserRepositoryImpl 实现 findAllActive，注入 UserPoMapper 完成 PO→Domain 映射
- [x] 1.5 新建 UserController，GET /api/users，调用 UserRepository.findAllActive 并返回 Result<List<UserResponse>>
- [x] 1.6 新建 UserResponse DTO（id + username）
- [x] 1.7 启动后端，用 curl 验证 GET /api/users 返回正确数据

## 2. 前端：Pinia Store

- [x] 2.1 新建 stores/user.js — useUserStore，fetchUsers action 调 GET /api/users，存储 users 列表
- [x] 2.2 新建 stores/project.js — useProjectStore，包含 list/create/detail/update/remove actions，映射所有枚举值（类型、状态）为中文标签

## 3. 前端：共享组件

- [x] 3.1 新建 components/UserSelect.vue — 封装 a-select，onMounted 调 useUserStore().fetchUsers()，v-model:value 绑定 projectManagerId
- [x] 3.2 新建 components/ProjectForm.vue — 表单复用组件，支持 mode="create"/"edit" 和 initialValues prop，含名称/类型/地点/甲方/监理/项目经理/日期/备注字段，Ant Design 表单校验规则

## 4. 前端：项目列表页

- [x] 4.1 新建 views/ProjectListPage.vue — a-table 展示项目列表，含搜索框（keyword）、类型下拉筛选（a-select）、状态下拉筛选（a-select）、新建按钮、分页器
- [x] 4.2 搜索筛选项与 URL query 参数同步（useRoute/useRouter）
- [x] 4.3 操作列：查看、编辑、删除按钮，删除带 a-modal 确认弹窗

## 5. 前端：项目新建页

- [x] 5.1 新建 views/ProjectCreatePage.vue — 使用 ProjectForm（mode="create"），提交调 projectStore.create()，成功跳转 /projects，取消返回 /projects

## 6. 前端：项目详情页

- [x] 6.1 新建 views/ProjectDetailPage.vue — 只读展示所有项目字段（使用 a-descriptions），加载中显示 Spin，不存在时显示错误提示
- [x] 6.2 底部操作按钮：编辑（跳转编辑页）、删除（确认弹窗后软删除并跳回列表）、返回列表

## 7. 前端：项目编辑页

- [x] 7.1 新建 views/ProjectEditPage.vue — 使用 ProjectForm（mode="edit"，initialValues 从 API 获取），提交调 projectStore.update()，成功跳转详情页，取消返回详情页
- [x] 7.2 状态流转支持：编辑页根据当前状态展示可选的目标状态下拉选项（前端维护流转映射表）

## 8. 前端：路由与导航

- [x] 8.1 router/index.js 新增 4 条路由：/projects、/projects/new、/projects/:id、/projects/:id/edit，全部设置 meta.requiresAuth: true
- [x] 8.2 DashboardPage.vue 顶栏菜单新增"项目管理"菜单项，点击导航至 /projects
