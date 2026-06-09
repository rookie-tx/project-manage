## Why

后端已具备完整的项目管理 API（CRUD + 下拉选择），Web SPA 也已实现项目管理的全部页面，但小程序端"项目管理"入口尚标注"即将上线"。项目管理是平台的核心功能——用户需要在小程序上查看项目信息、新建项目、编辑项目详情，这是移动办公的基础需求。Dashboard 的 5 个入口中仅剩这一个尚未激活。

## What Changes

- **小程序新增**：3 个项目页面——列表页（关键词搜索 + 类型/状态 chip 双筛选 + 卡片列表 + 分页）、表单页（新建/编辑复用，通过 `id` 参数区分模式）、详情页（字段展示 + 编辑/删除按钮）
- **小程序修改**：Dashboard 📋 项目管理入口从"即将上线"激活为可跳转
- 后端无需改动，所有 API 已就绪
- 权限控制：`project:create` 控制新建按钮，`project:update` 控制编辑按钮，`project:delete` 控制删除按钮

## Capabilities

### New Capabilities
- `miniapp-project-management`: 小程序项目管理。包含列表页（关键词搜索 + 类型/状态 chip 筛选 + 卡片列表 + 上下拉分页）、新建/编辑复用表单页（项目类型选择器、项目经理人选）、详情页（字段展示 + 权限控制的操作按钮）。

### Modified Capabilities
<!-- None -->

## Impact

- 后端：无改动
- 小程序：新建 `api/project.js` + 12 个页面文件（list/form/detail 各 4 个）；修改 `dashboard.js`、`app.json`
- 无破坏性变更
