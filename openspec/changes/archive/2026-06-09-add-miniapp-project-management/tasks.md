## 1. 小程序 API 模块

- [x] 1.1 新建 `api/project.js` — 封装项目管理 API（list / detail / create / update / del / getUsers），调用 `api/client.js` 的 request 方法

## 2. 项目列表页

- [x] 2.1 创建 `pages/project/list.js` — 实现关键词搜索、类型/状态 chip 筛选、`onReachBottom` 上拉加载、`onPullDownRefresh` 下拉刷新、权限控制新建按钮、点击卡片跳转详情
- [x] 2.2 创建 `pages/project/list.wxml` — 搜索框 + 类型 chip 横滚 + 状态 chip 横滚、项目卡片列表（编号、名称、类型标签、状态标签、甲方、项目经理）、空状态、加载状态
- [x] 2.3 创建 `pages/project/list.wxss` — 列表页样式
- [x] 2.4 创建 `pages/project/list.json` — 页面配置（开启 `enablePullDownRefresh`）

## 3. 项目表单页（新建/编辑复用）

- [x] 3.1 创建 `pages/project/form.js` — `id` 参数判断模式、编辑时加载已有数据预填、项目类型选择器（客户端枚举）、项目经理 picker、表单校验、提交时区分 POST/PUT、权限控制
- [x] 3.2 创建 `pages/project/form.wxml` — 表单布局（项目名称、类型选择器、地点、甲方、监理、项目经理选择器、计划日期、备注、状态选择器）、动态标题
- [x] 3.3 创建 `pages/project/form.wxss` — 表单页样式
- [x] 3.4 创建 `pages/project/form.json` — 页面配置

## 4. 项目详情页

- [x] 4.1 创建 `pages/project/detail.js` — 加载详情数据、权限控制按钮显隐、编辑跳转、删除操作
- [x] 4.2 创建 `pages/project/detail.wxml` — 头部（项目名称+编号）、信息网格（类型、地点、甲方、监理、项目经理、计划/实际日期、状态、备注、时间）、操作按钮区
- [x] 4.3 创建 `pages/project/detail.wxss` — 详情页样式
- [x] 4.4 创建 `pages/project/detail.json` — 页面配置

## 5. Dashboard 入口与路由注册

- [x] 5.1 修改 `pages/dashboard/dashboard.js` — 项目管理入口 `tip` 改为 `'点击进入'`，`url` 设为 `'/pages/project/list'`，`active` 设为 `true`
- [x] 5.2 修改 `app.json` — 在 `pages` 数组中注册三个新页面路径
