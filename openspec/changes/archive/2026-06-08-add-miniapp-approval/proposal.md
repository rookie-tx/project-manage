## Why

后端已具备完整的通用审批引擎（Flowable BPMN + 通用任务模块），Web SPA 也实现了待办任务列表和审批操作，但小程序端"待办审批"入口尚标注"即将上线"。审批是需要移动端即时处理的场景——管理者在外时需要通过手机查看待办任务、做出审批决策，这是移动办公的核心需求。

## What Changes

- **后端新增**：`GET /api/tasks/count` 接口，返回当前用户的待办任务数量，供小程序 Dashboard 角标使用
- **小程序新增**：2 个审批页面——待办列表页（卡片列表 + 上拉加载更多 + 下拉刷新）、审批操作页（任务信息展示 + 审批意见输入 + 通过/驳回按钮 + 底部审批历史时间线）
- **小程序修改**：Dashboard 待办审批入口从"即将上线"激活为可跳转，并显示待办数量角标；借助已有的 `auth.hasPermission('task:complete')` 控制审批按钮显隐

## Capabilities

### New Capabilities
- `miniapp-approval`: 小程序待办审批功能。包含待办任务列表（分页加载、下拉刷新）、审批操作（通过/驳回 + 审批意见）、审批历史时间线、Dashboard 入口激活及待办数量角标。

### Modified Capabilities
- `approval-task-management`: 新增 `GET /api/tasks/count` 端点，返回当前登录用户的待办任务总数，支持按 assignee 过滤。

## Impact

- 后端：`TaskController.java` 新增 `GET /api/tasks/count` 方法 + `TaskApplicationService.java` 新增 `countTasks()` 方法（约 15 行）
- 小程序：新建 2 个页面（list + action，共 8 个文件）+ 1 个 API 模块 `api/approval.js`；修改 `dashboard.js`、`app.json`
- 无破坏性变更，不影响现有 Web SPA 和后端审批逻辑
