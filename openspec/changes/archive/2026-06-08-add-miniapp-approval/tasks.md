## 1. 后端待办计数接口

- [x] 1.1 在 `TaskApplicationService` 中新增 `countTasks(String assignee)` 方法，复用 `WorkflowTaskRepository` 查询 Flowable 当前用户的待办任务总数
- [x] 1.2 在 `TaskController` 中新增 `GET /api/tasks/count` 端点，调用 `countTasks()` 返回 `{"count": N}`

## 2. 小程序 API 模块

- [x] 2.1 新建 `api/approval.js` — 封装待办审批 API（list / complete / history / count），调用 `api/client.js` 的 request 方法

## 3. 待办列表页

- [x] 3.1 创建 `pages/approval/list.js` — 实现 `onReachBottom` 上拉加载更多、`onPullDownRefresh` 下拉刷新、点击任务跳转审批操作页（将 task 字段编码到 URL 参数）
- [x] 3.2 创建 `pages/approval/list.wxml` — 任务卡片列表（业务标题、业务副标题、审批节点、到达时间）、空状态、加载状态
- [x] 3.3 创建 `pages/approval/list.wxss` — 列表页样式（卡片布局、时间标签样式）
- [x] 3.4 创建 `pages/approval/list.json` — 页面配置（开启 `enablePullDownRefresh`）

## 4. 审批操作页

- [x] 4.1 创建 `pages/approval/action.js` — 接收 URL 参数解析任务信息、加载审批历史时间线、权限控制按钮显隐（`task:complete`）、驳回意见必填校验、提交审批、返回列表页并标记刷新
- [x] 4.2 创建 `pages/approval/action.wxml` — 任务信息卡片、审批意见输入框、通过/驳回按钮、底部审批历史时间线
- [x] 4.3 创建 `pages/approval/action.wxss` — 审批操作页样式（信息卡片、时间线、按钮样式）
- [x] 4.4 创建 `pages/approval/action.json` — 页面配置

## 5. Dashboard 激活与路由注册

- [x] 5.1 修改 `pages/dashboard/dashboard.js` — 待办审批入口的 `tip` 改为 `'点击进入'`，`url` 设为 `'/pages/approval/list'`；`onShow` 时调用 count API 更新角标数字
- [x] 5.2 修改 `app.json` — 在 `pages` 数组中注册两个新页面路径
