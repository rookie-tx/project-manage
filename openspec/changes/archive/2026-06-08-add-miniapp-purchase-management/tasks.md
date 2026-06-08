## 1. 小程序 API 模块

- [x] 1.1 新建 `api/purchase.js` — 封装采购申请 API（list / detail / create / update / delete），调用 `api/client.js` 的 request 方法。复用审批相关 API 到已有的 `api/approval.js`（start process 和 history 已在任务审批模块中）

## 2. 采购申请列表页

- [x] 2.1 创建 `pages/purchase/list.js` — 实现项目筛选（picker）、状态驱动操作按钮（statusCode 分支）、`onReachBottom` 上拉加载、`onPullDownRefresh` 下拉刷新、跳转新建/详情/编辑页、提交审批和删除操作
- [x] 2.2 创建 `pages/purchase/list.wxml` — 项目筛选区、采购申请卡片列表（物料名称、项目名称、数量单位、到场时间、状态标签、操作按钮区）、空状态、加载状态
- [x] 2.3 创建 `pages/purchase/list.wxss` — 列表页样式（卡片布局、状态标签颜色、操作按钮排列）
- [x] 2.4 创建 `pages/purchase/list.json` — 页面配置（开启 `enablePullDownRefresh`）

## 3. 采购申请表单页（新建/编辑复用）

- [x] 3.1 创建 `pages/purchase/form.js` — `id` 参数判断模式、编辑时加载已有数据预填、项目 picker 数据加载、单位选择器（客户端枚举映射）、表单校验、提交时区分 POST/PUT、权限控制（`pr:create`/`pr:approve`）
- [x] 3.2 创建 `pages/purchase/form.wxml` — 表单布局（项目选择器、物料名称、规格、品牌、数量、单位选择器、到场时间、备注）、动态标题（新建/编辑）
- [x] 3.3 创建 `pages/purchase/form.wxss` — 表单页样式
- [x] 3.4 创建 `pages/purchase/form.json` — 页面配置

## 4. 采购申请详情页

- [x] 4.1 创建 `pages/purchase/detail.js` — 加载详情数据、加载审批历史时间线、加载用户列表（审批人选）、提交审批（至少选择一个审批人校验）、状态驱动按钮显隐、编辑/删除操作
- [x] 4.2 创建 `pages/purchase/detail.wxml` — 字段信息卡片、提交审批区（3 个审批人 picker + 确认按钮）、审批历史时间线、操作按钮区（编辑/删除）、状态条件渲染
- [x] 4.3 创建 `pages/purchase/detail.wxss` — 详情页样式（信息卡片、审批区、时间线、按钮）
- [x] 4.4 创建 `pages/purchase/detail.json` — 页面配置

## 5. 审批操作页修改

- [x] 5.1 修改 `pages/approval/action.js` — `onLoad` 时解析 `businessType`，当值为 `PURCHASE_REQUEST` 时设置 `showDetailLink: true` 和 `detailId: businessId`
- [x] 5.2 修改 `pages/approval/action.wxml` — 在任务信息卡片和审批意见之间添加"查看采购详情"条件链接

## 6. Dashboard 入口与路由注册

- [x] 6.1 修改 `pages/dashboard/dashboard.js` — 在 features 数组中加入 `{ icon: '📦', name: '采购申请', tip: '点击进入', url: '/pages/purchase/list' }`
- [x] 6.2 修改 `app.json` — 在 `pages` 数组中注册三个新页面路径
