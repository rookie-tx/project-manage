## 1. 后端权限透出

- [x] 1.1 修改 `WechatLoginResponse.UserInfo` — 增加 `List<String> permissions` 字段，修改 `success()` 静态工厂方法签名接收 permissions 参数
- [x] 1.2 修改 `WechatAuthController.wechatLogin()` 和 `wechatBind()` — 将 `buildLoginContext()` 返回的 `LoginContext.permissions()` 传入 `WechatLoginResponse.success()`

## 2. 小程序基础设施

- [x] 2.1 修改 `utils/auth.js` — 登录时存储用户 permissions；新增 `hasPermission(perm)` 方法判断是否拥有指定权限
- [x] 2.2 新建 `api/constructionWaste.js` — 封装废渣管理 API（list / create / getById / delete），调用 `api/client.js` 的 request 方法
- [x] 2.3 新建 `api/upload.js` — 封装 `wx.uploadFile` 为 Promise，携带 JWT Authorization 头和 businessType 参数，支持并发上传

## 3. 废渣列表页

- [x] 3.1 创建 `pages/waste/list.js` — 实现筛选逻辑（项目 picker 数据加载、废渣种类输入、日期选择）、`onReachBottom` 上拉加载更多、`onPullDownRefresh` 下拉刷新、点击记录跳转详情、权限控制"新建"按钮显隐
- [x] 3.2 创建 `pages/waste/list.wxml` — 顶部筛选区（项目 picker + 种类输入框 + 日期区间）、废渣记录卡片列表、底部加载状态提示
- [x] 3.3 创建 `pages/waste/list.wxss` — 列表页样式（卡片布局、筛选区样式、加载状态样式）
- [x] 3.4 创建 `pages/waste/list.json` — 页面配置（开启 `enablePullDownRefresh`）

## 4. 废渣创建页

- [x] 4.1 创建 `pages/waste/create.js` — 项目列表加载、表单数据绑定与校验、拍照/相册选择照片、`wx.uploadFile` 并发上传获取 fileIds、提交表单、错误处理
- [x] 4.2 创建 `pages/waste/create.wxml` — 表单布局（项目选择器、废渣种类输入框、数量/单位、车牌号/司机、处置去向、运出日期、备注）、照片上传区（缩略图预览 + 拍照/相册按钮）
- [x] 4.3 创建 `pages/waste/create.wxss` — 创建页样式（表单样式、照片预览网格样式）
- [x] 4.4 创建 `pages/waste/create.json` — 页面配置

## 5. 废渣详情页

- [x] 5.1 创建 `pages/waste/detail.js` — 接收页面参数 `id`、加载详情数据、展示关联照片缩略图、`wx.previewImage` 照片预览、`wx.showModal` 删除确认、权限控制删除按钮显隐
- [x] 5.2 创建 `pages/waste/detail.wxml` — 信息卡片（行式字段展示）、照片网格、底部删除按钮
- [x] 5.3 创建 `pages/waste/detail.wxss` — 详情页样式（字段行样式、照片网格样式、删除按钮样式）
- [x] 5.4 创建 `pages/waste/detail.json` — 页面配置

## 6. Dashboard 激活与路由注册

- [x] 6.1 修改 `pages/dashboard/dashboard.js` — 废渣管理入口的 `tip` 从 `'即将上线'` 改为 `'点击进入'`，`url` 设为 `'/pages/waste/list'`
- [x] 6.2 修改 `app.json` — 在 `pages` 数组中注册三个新页面路径
