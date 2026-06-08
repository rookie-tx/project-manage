## Why

后端已具备完整的建筑废渣管理 API（CRUD + 文件上传），Web SPA 也已实现废渣管理的全部页面，但小程序端废渣管理入口尚标注"即将上线"，用户无法在手机上查看和登记废渣运出记录。工地现场人员需要通过手机拍照、填表完成废渣运出登记，这是核心移动办公场景。

## What Changes

- **后端修改**：`WechatLoginResponse.UserInfo` 增加 `permissions` 字段，使小程序登录后直接获取用户权限列表，无需额外请求
- **小程序新增**：3 个废渣管理页面——列表页（含筛选和分页）、创建页（含拍照上传）、详情页（含照片预览和删除）
- **小程序修改**：Dashboard 废渣管理入口从"即将上线"激活为可跳转；auth.js 增加权限存储和 `hasPermission()` 方法
- **小程序新增**：废渣管理 API 模块与文件上传 API 模块，适配微信小程序的上传方式

## Capabilities

### New Capabilities
- `miniapp-waste-management`: 小程序建筑废渣运出记录管理。包含废渣列表（按项目/种类/日期筛选，上拉加载更多，下拉刷新）、废渣创建（拍照或相册上传照片，填写废渣种类、数量、运输信息等）、废渣详情（含照片预览，条件删除）。所有操作受后端权限控制。

### Modified Capabilities
- `wechat-auth`: 微信登录接口返回的用户信息中增加 `permissions` 字段，使小程序端无需额外请求即可获取当前用户的权限列表，用于前端 UI 权限控制（如隐藏无权限操作按钮）。

## Impact

- 后端：`WechatLoginResponse.java`、`WechatAuthController.java`（约 10 行改动）
- 小程序：新建 4 个文件模块（API: `constructionWaste.js`, `upload.js`；页面: `pages/waste/list`, `pages/waste/create`, `pages/waste/detail`），修改 `auth.js`、`dashboard.js`、`app.json`（共约 18 个文件）
- 无破坏性变更，不影响现有 Web SPA 和后端 API
