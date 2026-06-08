## Context

后端已有完整的建筑废渣管理 API（`ConstructionWasteController`），提供列表查询（支持按项目、废渣种类、日期范围筛选，分页 offset/limit）、创建（含文件关联回填）、详情查看（含项目名称和关联照片）、软删除。Web SPA 已实现完整的废渣管理页面（列表、创建、详情、删除），使用 Ant Design Vue 组件库。

小程序端目前仅有登录、Dashboard（含"废渣管理"占位入口标注"即将上线"）和考勤打卡三个页面。小程序使用微信原生框架（`.js` / `.wxml` / `.wxss` / `.json`），非 uni-app——虽然早期设计文档规划为 uni-app，但实际实现是原生框架。

微信登录接口 `POST /api/auth/wechat-login` 返回的 `WechatLoginResponse.UserInfo` 当前仅包含 `id` 和 `username`，不包含 `permissions`，导致小程序端无法在前端做权限判断（如是否显示"新建"按钮、"删除"按钮）。但后端 `WechatAuthController.buildLoginContext()` 实际上已查询了角色和权限数据，只是未透出到响应。

## Goals / Non-Goals

**Goals:**
- 小程序端提供废渣运出记录的完整移动操作：列表查看（含筛选 + 上拉加载更多 + 下拉刷新）、新建记录（含拍照/相册上传 → wx.uploadFile → 获取 fileId）、详情查看（含照片预览）、删除记录
- 小程序登录后自动获取用户权限列表，前端根据权限控制 UI 元素的显隐（`waste:create` 控制"新建"按钮，`waste:delete` 控制"删除"按钮）
- 后端 `WechatLoginResponse.UserInfo` 补充 `permissions` 字段，微信登录接口返回完整用户权限
- 文件上传适配微信小程序：封装 `wx.uploadFile` 为 Promise，自动携带 JWT 认证头

**Non-Goals:**
- 不修改废渣管理业务 API（创建/列表/详情/删除接口均不变）
- 不修改 Web SPA 的废渣管理功能
- 不新增文件上传 API（复用现有 `POST /api/files/upload`）
- 不修改数据库表结构
- 不包含废渣记录的编辑/修改功能（Web 端也无此功能）
- 不包含离线提交/草稿保存

## Decisions

### 决策 1：小程序页面结构 — 3 个独立页面

**选择**：在 `pages/waste/` 下新建 `list`、`create`、`detail` 三个独立页面，每个页面 4 个文件（`.js` / `.wxml` / `.wxss` / `.json`）。

```
pages/waste/
├── list.js          # 列表页逻辑：筛选、分页、跳转
├── list.wxml        # 列表页模板：筛选区 + 卡片列表
├── list.wxss        # 列表页样式
├── list.json        # 页面配置
├── create.js        # 创建页逻辑：表单、拍照、上传、提交
├── create.wxml      # 创建页模板：表单 + 照片预览区
├── create.wxss      # 创建页样式
├── create.json      # 页面配置
├── detail.js        # 详情页逻辑：数据加载、照片预览、删除
├── detail.wxml      # 详情页模板：字段展示 + 照片网格
├── detail.wxss      # 详情页样式
└── detail.json      # 页面配置
```

**理由**：与现有 `pages/login/`、`pages/checkin/` 结构一致，遵循小程序原生框架约定。每个页面职责单一，便于维护。

**备选方案**：合并列表和详情为单页（通过状态切换）。拒绝——小程序的页面栈管理（`wx.navigateTo`/`wx.navigateBack`）天然适合独立页面，单页模式会使状态管理复杂化。

### 决策 2：照片上传 — 两步分离式

**选择**：

```
① wx.chooseImage({ sourceType: ['album', 'camera'] })
   → 获取临时文件路径数组 tempFilePaths[]
② 逐文件 Promise.all(wx.uploadFile({ filePath, name: 'file', ... }))
   → POST /api/files/upload，携带 JWT Authorization 头
   → 获取 fileId[] 数组
③ 提交表单 POST /api/construction-wastes { ...fileIds }
```

**理由**：微信小程序不支持 Web 的 FormData 一次性提交多文件+表单数据。必须先上传获取 fileId，再提交业务表单。后端现有 API 已支持此流程（`CreateConstructionWasteRequest.fileIds`）。

**关键实现细节**：
- `wx.uploadFile` 不支持全局 HTTP 封装（它有自己的 `header` 参数），需要独立的 `api/upload.js` 模块手动拼接 `Authorization: Bearer <token>`
- 支持多文件并发上传（`Promise.all`），但需限制单次最多 9 张（`wx.chooseImage` 默认上限）
- 上传过程中显示进度反馈

### 决策 3：列表分页 — 上拉触底 + 下拉刷新

**选择**：使用小程序的 `onReachBottom()` 实现滚动加载更多，`onPullDownRefresh()` 实现下拉刷新，不使用传统分页器。

```
第一页：GET /api/construction-wastes?offset=0&limit=20
触底加载下一页：GET /api/construction-wastes?offset=20&limit=20
下拉刷新：重置列表，重新从 offset=0 加载
```

**列表状态管理**：
- `records[]`：已加载的记录列表
- `hasMore`：是否还有更多数据
- `loading`：加载中（首次加载用全屏 loading，追加加载用底部 loading 提示）
- `refreshing`：下拉刷新中

**理由**：移动端主流交互模式，与微信小程序原生能力契合。后端 `offset/limit` 已就绪，无需改动。

### 决策 4：项目选择器 — 微信原生 picker + 名称展示

**选择**：使用 `<picker mode="selector" range="{{projectNames}}">` 展示项目名称列表，用户选择后通过 index 获取 `projectId`。

```javascript
// 数据结构
projects: [{ id: 1, name: '香蜜湖项目', code: 'XM-001' }, ...]
projectNames: ['香蜜湖项目', '南山科技园', ...]  // picker range 用

// 选择时
onProjectChange(e) {
  const idx = e.detail.value
  this.setData({
    selectedProjectIdx: idx,
    projectId: this.data.projects[idx].id
  })
}
```

**理由**：微信小程序 `<picker>` 组件仅支持单列纯文本数组，无法直接展示对象列表。通过维护两个并列数组（`projects` + `projectNames`），索引对齐，选择时通过 index 获取完整对象。

### 决策 5：废渣种类 — 自由输入

**选择**：使用 `<input>` 自由输入，不预设选项列表。

**理由**：用户明确选择"自由输入"。与 Web 端保持一致——Web SPA 也是 `<a-input>` 自由输入，placeholder 示例为"如：混凝土块、砖块、土方"。

### 决策 6：权限透出 — 修改 WechatLoginResponse

**选择**：在 `WechatLoginResponse.UserInfo` 中增加 `List<String> permissions` 字段，修改 `success()` 静态工厂方法签名，`WechatAuthController.wechatLogin()` 和 `wechatBind()` 中将 `LoginContext.permissions()` 传入。

```java
// 修改前
resp.user = new UserInfo(userId, username);

// 修改后
resp.user = new UserInfo(userId, username, permissions);
```

**小程序存储**：登录成功后 `auth.setAuth(token, user)` 同时存储 `permissions`；`auth.js` 新增 `hasPermission(perm)` 方法：

```javascript
function hasPermission(perm) {
  const user = getUser()
  return user && user.permissions && user.permissions.includes(perm)
}
```

**理由**：
- `WechatAuthController.buildLoginContext()` 已查询 role 和 permission 数据（第 129-140 行），不额外增加数据库查询
- `LoginContext` record 已携带 permissions，只需透出到响应即可
- 避免小程序端额外发起一次 `/api/auth/me` 请求来获取权限
- 与 Web SPA `useAuthStore.hasPermission()` API 保持一致

**备选方案**：小程序调用 `/api/auth/me` 获取权限。拒绝——多一次网络请求，影响启动速度。

### 决策 7：删除操作 — 确认弹窗

**选择**：详情页底部"删除记录"按钮，点击后使用 `wx.showModal` 确认弹窗，确认后调用 API。

```javascript
wx.showModal({
  title: '确认删除',
  content: '确定要删除该运出记录吗？关联的照片将一并删除。',
  confirmText: '确认删除',
  confirmColor: '#ff4d4f',
  success: async (res) => {
    if (res.confirm) {
      await wasteApi.del(id)
      wx.showToast({ title: '已删除', icon: 'success' })
      wx.navigateBack()
    }
  }
})
```

**理由**：与 Web SPA 的 `Modal.confirm` 对齐。小程序 `wx.showModal` 是唯一的模态弹窗 API。

## Risks / Trade-offs

- **[上传失败处理]** 并发上传多个文件时，部分文件上传失败 → 已上传的文件会成为"孤儿"（`business_id` 为 NULL）。当前后端无孤儿文件自动清理机制。→ 短期内孤儿文件不影响业务（不关联的记录不可见），后续可增加定时清理任务。
- **[照片预览跨域]** 后端文件访问 URL 可能因 CORS 策略在小程序图片组件中无法加载 → 使用 `wx.previewImage` 直接打开大图（不经过 CORS 检查），`<image>` 标签加载预览小图时依赖后端已配置的 CORS。
- **[JWT 权限时效]** 权限存储在 JWT 中，管理员变更用户权限后需等 Token 过期（7 天）或用户重新登录才生效 → 企业内部管理场景可接受，后续可增加短期 token + refresh 机制。
- **[picker 项目列表过长]** 项目数量较多时，微信原生 picker 滚动选择体验下降 → 当前项目数量有限（企业管理场景，通常 < 50 个），可接受。后续可改为搜索式的自定义选择器。
- **[wx.uploadFile 并发限制]** 微信小程序同时上传文件数限制（通常 10 个并发），多文件时需控制并发数 → 限制单次最多 9 张照片，`Promise.all` 在正常范围内安全。
