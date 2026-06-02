## Context

后端项目管理 API 已开发完成并遵循 DDD 四层架构，提供 5 个端点：创建、列表查询、详情、更新、软删除。前端目前仅有登录和 Dashboard 骨架页，使用 Vue 3 Composition API + Ant Design Vue 4 + Pinia 3 + Vue Router 5。

本次设计在前端新增项目管理 UI，以及后端新增一个用户列表端点支撑项目经理下拉选择。前端开发严格遵循已有的项目规范：`<script setup>`、Setup Store、axios 拦截器、scoped CSS、中文命名。

## Goals / Non-Goals

**Goals:**
- 用户可通过列表页查看、搜索、筛选、分页浏览项目
- 用户可新建项目（表单校验）
- 用户可查看项目详情
- 用户可编辑项目（表单回填、状态流转）
- 用户可软删除项目（确认弹窗）
- 项目经理字段通过下拉选择器选择活跃用户
- Dashboard 顶栏菜单新增"项目管理"入口

**Non-Goals:**
- 项目与采购、仓库、成本模块的关联（后续）
- 批量操作（批量删除、批量导出）
- 权限控制（角色区分）
- 文件上传
- 操作日志/审计

## Decisions

### 1. 页面拆分策略：4 个独立页面 vs 单页弹窗

**选择：4 个独立页面**

| 路由 | 页面 | 说明 |
|------|------|------|
| `/projects` | ProjectListPage | 列表 + 搜索 + 分页 |
| `/projects/new` | ProjectCreatePage | 新建表单 |
| `/projects/:id` | ProjectDetailPage | 只读详情 + 操作按钮 |
| `/projects/:id/edit` | ProjectEditPage | 编辑表单 |

**理由：** 每个页面职责单一，URL 可直接分享，符合 Vue Router 惯用模式。新建和编辑共用 `ProjectForm.vue` 组件避免表单重复。

**备选方案：** 列表页用弹窗做新建/编辑（减少页面数），但弹窗内表单空间局促，编辑时 URL 不可分享，且与项目现有页面跳转模式不一致。

### 2. 项目表单复用：ProjectForm.vue

**选择：** 抽取 `ProjectForm.vue` 组件，新建和编辑页面共用。

通过 props 区分模式：
- `mode="create"` — 空表单，提交调用 `projectStore.create()`
- `mode="edit"` — 回填数据（通过 `initialValues` prop），提交调用 `projectStore.update()`

**理由：** 新建和编辑的表单字段、校验规则完全一致，避免两份代码。

### 3. 用户选择器：独立组件 UserSelect.vue

**选择：** `UserSelect.vue` 封装 Ant Design 的 `<a-select>`，`onMounted` 时调用 `useUserStore().fetchUsers()` 获取活跃用户列表。

```vue
<UserSelect v-model:value="formState.projectManagerId" />
```

**理由：** 复用（新建和编辑都需要），职责单一，替换/扩展方便（后续若有角色筛选只需改一处）。

### 4. 用户列表后端：DDD 四层 vs 最简实现

**选择：最简实现 — Controller + Mapper，不新建独立模块。**

复用 `cn.tx.aiagent.login` 包下已有的 `UserRepository`、`UserMapper`、`User` 领域实体。新增：
- `UserMapper.xml` 中增加 `selectAllActive` SQL
- `UserRepository` 中增加 `findAllActive()` 方法
- `UserRepositoryImpl` 实现新方法
- `LoginDomainConfig` 无需修改（UserRepository 非新领域服务）
- 新建 `UserController`（GET `/api/users`）直接在 api 层调用 `UserRepository`

**理由：** 用户查询不涉及复杂业务逻辑，无需 DomainService/ApplicationService。DDD 分层不为简单查询增加不必要的抽象。

**备选方案：** 严格按照 DDD 四层新建 `user` 模块（domain → infrastructure → application → api），但对于一个"查列表返回 id+username"的接口过于繁重。

### 5. API 调用放置：Store 层 vs 组件层

**选择：** Store 层（`useProjectStore`、`useUserStore`）

所有 API 调用封装在 Pinia store action 中，组件只调用 action 和读取 state。遵循项目已有的 `useAuthStore` 模式。

### 6. 状态流转 UI

**选择：** 编辑页根据当前状态展示可流转的目标状态按钮。

```
详情页/编辑页展示状态标签 + 可操作的状态流转按钮：
┌──────────────────────────────────────────────────┐
│  当前状态: [在建]                                  │
│                                                    │
│  可流转至: [暂停] [竣工]                            │
└──────────────────────────────────────────────────┘
```

前端维护一份与后端 `ProjectStatus` 一致的状态流转映射表，只展示合法目标状态。执行流转时调用 `PUT /api/projects/{id}` 更新 status 字段。

### 7. 页码与筛选：URL 查询参数同步

**选择：** 搜索关键词、类型筛选、状态筛选、页码均通过 URL query 参数同步（`useRoute`/`useRouter`）。

**理由：** 刷新保持状态，浏览器前进/后退可用，URL 可分享。

## Risks / Trade-offs

- **用户列表接口简单但非标准 DDD**：`GET /api/users` 直接在 api 层调用 Repository，跳过了 Application/Domain 层。对于纯读列表这是可接受的。若后续用户列表需要复杂筛选或权限控制，可重构为完整 DDD 模块。
- **N+1 项目列表查询**：后端 `ProjectApplicationService.toResponse()` 对每个 project 逐条查 `UserRepository.findById()`。当前项目量级小，影响可忽略。数据量增长后需改为批量查询 (`findByIds`)，但这不在本次范围内。
- **状态流转映射前端维护**：前后端需保持状态流转规则一致。若后端 `ProjectStatus` 枚举变更，前端需同步更新。可接受——状态流转规则变更频率极低。
