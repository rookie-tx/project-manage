## Why

后端项目管理 API（CRUD + 列表查询 + 软删除）已开发完成，但前端缺少对应的管理界面，用户无法通过浏览器操作项目数据。需要在 `ai-agent-client` 中新增项目管理前端页面，同时后端新增用户列表接口以支撑项目经理选择器。

## What Changes

### 后端（小改动）
- 新增 `GET /api/users` 接口，返回所有活跃用户（id + username），供前端用户下拉选择器使用

### 前端（主要改动）
- 新增 `stores/project.js` — 项目管理 Pinia store（列表、创建、详情、更新、删除）
- 新增 `stores/user.js` — 用户 Pinia store（获取活跃用户列表）
- 新增 `views/ProjectListPage.vue` — 项目列表页（搜索、筛选、分页、操作按钮）
- 新增 `views/ProjectCreatePage.vue` — 新建项目页（表单提交）
- 新增 `views/ProjectDetailPage.vue` — 项目详情页（只读展示 + 状态流转操作）
- 新增 `views/ProjectEditPage.vue` — 编辑项目页（表单回填 + 更新提交）
- 新增 `components/ProjectForm.vue` — 项目表单复用组件（新建 + 编辑共用）
- 新增 `components/UserSelect.vue` — 用户下拉选择器复用组件
- 修改 `router/index.js` — 新增 4 条路由（`/projects`、`/projects/new`、`/projects/:id`、`/projects/:id/edit`）
- 修改 `views/DashboardPage.vue` — 顶栏菜单新增"项目管理"入口

## Capabilities

### New Capabilities
- `project-management-ui`: 前端项目管理 CRUD 界面，包括列表（搜索/筛选/分页）、新建、详情查看、编辑、软删除，以及状态流转操作
- `user-list-api`: 后端新增 `GET /api/users` 接口，返回活跃用户列表供下拉选择器使用

### Modified Capabilities
- 无

## Impact

### 影响的后端文件
- `ai-agent-server/src/main/java/cn/tx/aiagent/login/` — 新增 UserController / UserApplicationService / UserListResponse，UserMapper.xml 新增查询，UserRepository 新增 `findAllActive()` 方法

### 影响的前端文件（新增）
- `ai-agent-client/src/stores/project.js`
- `ai-agent-client/src/stores/user.js`
- `ai-agent-client/src/views/ProjectListPage.vue`
- `ai-agent-client/src/views/ProjectCreatePage.vue`
- `ai-agent-client/src/views/ProjectDetailPage.vue`
- `ai-agent-client/src/views/ProjectEditPage.vue`
- `ai-agent-client/src/components/ProjectForm.vue`
- `ai-agent-client/src/components/UserSelect.vue`

### 影响的前端文件（修改）
- `ai-agent-client/src/router/index.js` — 新增路由
- `ai-agent-client/src/views/DashboardPage.vue` — 新增菜单项
