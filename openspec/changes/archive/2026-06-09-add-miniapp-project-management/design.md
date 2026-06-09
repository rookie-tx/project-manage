## Context

后端已有完整的项目管理模块（`cn.tx.aiagent.project`），提供标准 CRUD API。`GET /api/projects` 支持 keyword（名称模糊搜索）、type、status 多条件筛选和分页。项目类型枚举（房建/市政/装修/路桥/水利/其他）和状态枚举（筹备中/在建/停工/已竣工/已结算）在后端已定义。项目经理通过 `GET /api/users/select` 获取用户列表选择。

Web SPA 已实现完整项目管理功能：列表页（搜索+筛选+分页表）、新建/编辑表单（含项目类型下拉和项目经理搜索选择）、详情页。

小程序 Dashboard 中"项目管理"入口当前标注"即将上线"。本次变更完全复用已有的 API 和已验证的前端模式（采购申请的 form 复用模式、废渣管理的 chip 筛选模式）。

## Goals / Non-Goals

**Goals:**
- 小程序提供项目管理的完整移动操作：列表查看（关键词搜索+类型/状态筛选）、新建、编辑（复用表单页）、详情查看、删除
- Dashboard 项目管理入口激活
- 权限控制：`project:create`/`project:update`/`project:delete` 控制按钮显隐

**Non-Goals:**
- 不修改后端 API
- 不修改 Web SPA
- 不包含项目状态流转逻辑的 UI（状态可在编辑页通过枚举选择器修改，后端校验流转合法性）

## Decisions

### 决策 1：表单页复用 — 与采购申请一致

**选择**：`pages/project/form` 通过 `id` 参数区分新建/编辑模式，与 `pages/purchase/form` 模式完全一致。新建模式提交 `POST /api/projects`，编辑模式提交 `PUT /api/projects/{id}`。

### 决策 2：项目经理选择器

**选择**：调用 `GET /api/users/select` 获取用户列表，用微信原生 `<picker>` 展示用户名。与采购申请详情页的审批人选模式一致。

### 决策 3：列表筛选 — 关键词搜索 + chip 双筛选

**选择**：顶部搜索框（输入框 + 搜索按钮）+ 下方 chip 横滚类型筛选和状态筛选。与废渣管理的 chip 筛选风格一致。所有筛选参数通过 API 的 `keyword`、`type`、`status` 参数传递。

### 决策 4：项目类型/状态 — 客户端枚举映射

**选择**：JS 中维护类型和状态枚举（与后端 `ProjectType`/`ProjectStatus` 一致），列表页筛选和表单页选择器均使用客户端枚举。

### 决策 5：权限控制

**选择**：`auth.hasPermission('project:create')` 控制新建按钮，`auth.hasPermission('project:update')` 控制编辑按钮，`auth.hasPermission('project:delete')` 控制删除按钮。与废渣、采购等页面权限模式一致。

## Risks / Trade-offs

- **[状态流转校验]** 编辑页修改状态时后端校验流转合法性，前端不预设流转规则 → 后端返回错误时展示错误信息即可
- **[项目经理列表]** `/api/users/select` 可能返回大量用户 → picker 组件滚动选择，可接受
