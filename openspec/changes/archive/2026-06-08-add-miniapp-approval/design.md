## Context

后端已有完整的通用任务审批模块（`cn.tx.aiagent.task`），基于 Flowable BPMN 引擎。API 包括待办列表 `GET /api/tasks`、任务完成 `POST /api/tasks/{id}/complete`、审批历史 `GET /api/tasks/history`、流程启动 `POST /api/tasks/start`。`TaskResponse` 返回通用字段（taskId、businessType、businessId、businessTitle、businessSubtitle、nodeName、createTime），不暴露业务专属字段。

Web SPA 已实现 `TaskListPage.vue`（表格 + 分页器 + 审批弹窗）和 `ApprovalHistory.vue`（时间线组件）。小程序目前 Dashboard 中"待办审批"入口标注"即将上线"。

认证体系已统一收口到 `LoginContextHolder`，JWT 和 Session 均可正确获取当前用户。`GET /api/tasks` 通过 `LoginContextHolder.getUserId()` 获取当前用户 ID 作为 assignee 查询条件。

## Goals / Non-Goals

**Goals:**
- 小程序提供审批人视角的完整移动审批操作：待办列表（分页 + 刷新）、审批处理（通过/驳回 + 意见）、审批历史查看
- 后端提供待办计数接口供 Dashboard 角标使用
- Dashboard 入口激活并显示待办数量
- 权限控制：无 `task:complete` 权限时隐藏审批按钮

**Non-Goals:**
- 不包含提交审批（`POST /api/tasks/start`）——这是"申请人视角"，属于业务模块（采购申请等），不在此次范围
- 不包含 Web SPA 的修改
- 不包含审批流程定义（BPMN）的修改
- 不包含消息推送提醒
- 审批操作页不展示业务详情——仅展示任务的通用信息。跳转到业务详情页属于后续迭代

## Decisions

### 决策 1：页面结构 — 2 个独立页面

**选择**：`pages/approval/list`（待办列表）和 `pages/approval/action`（审批操作），每页 4 个文件（`.js` / `.wxml` / `.wxss` / `.json`）。

任务数据通过 URL 查询参数传递：
```
list → action?taskId=xxx&businessType=xxx&businessId=xxx&businessTitle=xxx&...
```

将 `TaskResponse` 的关键字段编码到 URL 参数中，action 页直接解析，避免额外请求。

**备选方案**：action 页接收 taskId 后自己调 API 获取详情。拒绝——后端没有单任务查询接口，且多余的 HTTP 请求影响体验。

### 决策 2：审批历史 — 嵌入 action 页底部

**选择**：action 页加载时同时请求 `GET /api/tasks/history?businessType=X&businessId=X`，以纵向时间线形式展示在审批操作区下方。

**备选方案**：独立的历史页面。拒绝——审批人需要看到"前面的人怎么批的"再做决定，分页会增加操作成本。

### 决策 3：后端待办计数 — 新增独立端点

**选择**：新增 `GET /api/tasks/count` 返回 `{"count": N}`，在 `TaskApplicationService` 中新增 `countTasks(assignee)` 方法，复用 `WorkflowTaskRepository` 查询 Flowable 待办任务数。

```java
// TaskController.java
@GetMapping("/count")
public Result<Map<String, Long>> count() {
    Long userId = LoginContextHolder.getUserId();
    String assignee = userId != null ? String.valueOf(userId) : "0";
    long count = taskApplicationService.countTasks(assignee);
    return Result.success(Map.of("count", count));
}
```

**备选方案**：在列表接口的响应中附带 `total` 字段。拒绝——列表接口当前返回裸数组，改为包装对象会破坏 Web SPA 的现有契约。

### 决策 4：Dashboard 角标 — 主动拉取 + 简单展示

**选择**：Dashboard `onShow` 时调用 `GET /api/tasks/count`，在"待办审批"卡片右上角叠加红色数字角标。未登录时不请求；网络错误时角标不显示。

```
┌──────────┐
│  ✅  ③   │  ← 右上角红色角标
│ 待办审批  │
│ 点击进入  │
└──────────┘
```

**备选方案**：WebSocket 推送实时更新。拒绝——过度设计，拉取模式对审批场景足够（用户手动刷新或进入 Dashboard 时更新）。

### 决策 5：审批按钮 — 权限控制

**选择**：action 页读取 `auth.hasPermission('task:complete')`，无权限时隐藏"通过""驳回"按钮，仅展示任务信息和审批历史。

**理由**：与废渣管理的权限控制模式一致（`waste:delete` → 控制删除按钮显隐）。后端 `POST /api/tasks/{taskId}/complete` 已有 `@RequirePermission("task:complete")`，前端控制是防御性 UI 优化。

### 决策 6：列表分页 — 与废渣列表一致

**选择**：复用废渣列表的分页模式——`onReachBottom` 触底加载更多，`onPullDownRefresh` 下拉刷新。`page` 从 1 开始递增，每页 20 条。

**备选方案**：传统分页器。拒绝——移动端触底加载是主流交互。

## Risks / Trade-offs

- **[taskId URL 编码]** taskId 为 Flowable 生成的 UUID，可能含特殊字符 → 使用 `encodeURIComponent` 编码，action 页用 `decodeURIComponent` 解码
- **[计数接口权限]** `/api/tasks/count` 无显式 `@RequirePermission`，依赖隐式用户上下文 → 未登录时返回 0，与列表接口行为一致
- **[审批意见非必填]** 驳回时意见应为必填 → 前端校验——驳回时意见为空则提示"驳回须填写审批意见"，通过时意见选填。后端不做强制校验
- **[并发审批]** 两个审批人可能同时对同一任务操作 → Flowable 引擎保证任务只能完成一次，后续操作会抛出异常。前端展示后端返回的错误信息即可
