## Context

后端已有完整的采购申请模块（`cn.tx.aiagent.purchaserequest`），提供标准 CRUD API 以及通过 task 模块的审批流集成。API 包括列表（分页+项目筛选）、创建（`pr:create` 权限）、详情、更新（仅"待提交"状态可编辑，`pr:approve` 权限）、软删除。提交审批使用通用 `POST /api/tasks/start` 端点，审批历史使用通用 `GET /api/tasks/history` 端点。

Web SPA 已实现完整采购申请功能：列表页（项目筛选+分页表+状态驱动按钮）、新建表单、详情页（含审批历史时间线+提交审批弹窗）、编辑表单。

小程序已有审批列表页和审批操作页，但采购申请业务页面完全缺失。本次变更复用这些已有的架构模式（废渣管理的列表/表单模式、审批系统的跳转链路）。

## Goals / Non-Goals

**Goals:**
- 小程序提供采购申请的完整移动操作：列表查看（项目筛选+状态驱动按钮）、新建、编辑（复用表单页）、详情查看、提交审批（审批人选）、审批历史时间线
- 审批操作页支持跳转到采购申请详情（当 businessType 为 PURCHASE_REQUEST 时）
- Dashboard 新增采购申请功能入口
- 权限控制：`pr:create` 控制新建按钮，`pr:approve` 控制编辑/提交审批按钮

**Non-Goals:**
- 不修改后端 API
- 不修改 Web SPA
- 不包含审批流程定义的修改
- 不包含文件/图片上传

## Decisions

### 决策 1：表单页复用 — 单页面，`id` 参数区分模式

**选择**：`pages/purchase/form` 同时处理新建和编辑。`onLoad` 时检查 `options.id`：有 `id` = 编辑模式（加载已有数据预填），无 `id` = 新建模式。

```
/pages/purchase/form          → 新建，空白表单
/pages/purchase/form?id=123   → 编辑，预填数据
```

编辑模式下提交调用 `PUT /api/purchase-requests/{id}`，新建模式下调用 `POST /api/purchase-requests`。

**理由**：节省代码量，新建和编辑的表单结构完全一致。与废渣管理的 create 页模式不同——采购申请有编辑需求所以需要复用。

### 决策 2：提交审批 — 嵌入详情页

**选择**：在详情页底部内嵌审批人选区域（3 个 picker：部门经理、财务审批、总经理），用户选择后点击"确认提交"，调用 `POST /api/tasks/start`。

```javascript
// 提交审批
await client.post('/tasks/start', {
  businessType: 'PURCHASE_REQUEST',
  businessId: id,
  variables: {
    deptManagerId: selectedDeptManager,
    financeManagerId: selectedFinanceManager,
    generalManagerId: selectedGeneralManager
  }
})
```

审批人数据来自 `GET /api/users/select`。

**备选方案**：独立提交审批弹窗/页面。拒绝——移动端选择审批人的场景简单，直接内嵌在详情页即可，减少页面跳转。

### 决策 3：单位选择器 — 客户端枚举映射

**选择**：在 JS 中维护单位映射表（与 Web SPA 的 `UNIT_MAP` 一致），使用微信原生 `<picker>` 展示选项。

```javascript
const UNITS = [
  { code: 0, label: '个' }, { code: 1, label: '台' },
  { code: 2, label: '套' }, { code: 3, label: '米' },
  { code: 4, label: '吨' }, { code: 5, label: '立方米' },
  { code: 6, label: '卷' }, { code: 7, label: '桶' },
  { code: 8, label: '箱' }, { code: 9, label: '其他' }
]
```

**理由**：单位是固定枚举，不需要从后端动态获取。与 Web SPA 保持一致。

### 决策 4：状态驱动操作按钮

**选择**：列表卡片和详情页根据 `statusCode` 显示不同按钮组合。

| statusCode | 列表操作 | 详情操作 |
|-----------|---------|---------|
| 0 待提交 | 提交审批、编辑、删除 | 提交审批区、编辑、删除 |
| 1 审批中 | 查看 | 仅查看 |
| 2 已通过 | 查看 | 仅查看 |
| 3 已驳回 | 查看 | 仅查看 |

列表页每个卡片底部显示操作区，详情页顶部和底部各有按钮区。

### 决策 5：审批操作页跳转链接

**选择**：在 `action.js` 的 `onLoad` 中解析 `businessType`，当值为 `PURCHASE_REQUEST` 时设置 `showDetailLink: true` 和 `detailUrl`。WXML 中条件渲染"查看采购详情"链接。

```xml
<view wx:if="{{showDetailLink}}" class="detail-link" bindtap="handleViewDetail">
  <text>查看采购详情 ›</text>
</view>
```

跳转 URL：`/pages/purchase/detail?id={{businessId}}`

### 决策 6：列表分页 — 与已有模式一致

**选择**：`onReachBottom` 触底加载更多，`onPullDownRefresh` 下拉刷新。后端列表接口返回 `{ items, total, page, size }`，前端根据 `items.length` 判断 `hasMore`。

项目筛选使用微信原生 `<picker>` + 项目列表，选择后重置页码。

## Risks / Trade-offs

- **[单位映射同步]** 前端硬编码单位枚举 → 后端修改单位定义时需同步更新。规避：单位是稳定枚举，可接受维护成本
- **[审批人选加载]** `/api/users/select` 可能返回大量用户 → 小程序 picker 组件支持滚动，可接受
- **[编辑状态校验]** 编辑模式下若后端返回非"待提交"状态，前端应展示只读模式并隐藏提交按钮 → `onLoad` 时先检查 statusCode
- **[表单页模式切换]** 用户在编辑页返回后再进入新建页，需确保表单数据清空 → `onLoad` 每次都根据 `id` 参数初始化
- **[Dashboard 第 5 个入口]** Flex wrap 布局下第 5 个入口独占一行，视觉上不对称 → 可接受，后续可调整为 3 列布局
