# ai-agent-miniapp 微信小程序 — 项目规范

## 项目概述

智能管理平台配套微信小程序，为现场施工人员提供移动端操作入口。

| 属性 | 说明 |
|------|------|
| 框架 | 原生微信小程序 (WXSS/WXML/JS) |
| 运行环境 | 微信客户端 |
| 模块化 | CommonJS (`require`/`module.exports`) |
| 后端 API | `https://legwork-blush-vitally.ngrok-free.dev/api` |

---

## 目录结构

```
ai-agent-miniapp/
├── api/                    # API 层 — 按业务模块导出的请求函数
│   ├── client.js           # 统一 wx.request 封装 (token/错误处理)
│   ├── auth.js             # 登录/绑定
│   ├── approval.js         # 审批任务
│   ├── attendance.js       # 考勤打卡
│   ├── project.js          # 项目管理
│   ├── purchase.js         # 采购申请
│   ├── purchaseOrder.js    # 采购管理
│   ├── constructionWaste.js # 建筑废渣
│   └── upload.js           # 文件上传
├── utils/                  # 工具模块
│   ├── auth.js             # 认证状态管理 (token/user/权限)
│   ├── storage.js          # 持久化存储封装 (wx.getStorageSync)
│   └── location.js         # GPS 定位 + 模拟定位检测
├── pages/                  # 页面 — 每个页面 4 文件
│   ├── login/              # 微信登录 + 账号绑定
│   ├── dashboard/          # 首页工作台
│   ├── checkin/            # 考勤打卡
│   ├── project/            # 项目管理 (列表/新建编辑/详情)
│   ├── approval/           # 待办审批 (列表/操作)
│   ├── purchase/           # 采购申请 (列表/新建编辑/详情)
│   ├── purchase-order/     # 采购管理 (列表/新建编辑/详情)
│   └── waste/              # 建筑废渣 (列表/新建/详情)
├── app.js                  # 入口 — onLaunch 检查登录状态
├── app.json                # 全局配置 — pages 注册、window、permission
└── app.wxss                # 全局样式 — page 级别背景/字体
```

---

## API 层规范

### client.js — 统一请求封装

```javascript
const client = require('../api/client')

// GET/POST/PUT/DELETE 均通过 client 调用，不直接使用 wx.request
client.get('/projects', { page: 1, size: 20 })
client.post('/projects', payload)
client.put('/projects/1', payload)
client.del('/projects/1')
```

**核心规则：**
- `code === 0` → 自动 resolve `data` 字段（已解包）
- `code !== 0` → reject `Error(msg)`
- 认证错误 (`code === 2001 \|\| code === 2004`) → 清除 auth → `wx.reLaunch` 跳登录
- 自动附加 `Authorization: Bearer <token>`（双保险：内存优先，storage 兜底）
- GET 请求的 `params` 自动拼接为 query string

### 业务 API 模块模式

每个业务模块按 CRUD 导出函数，使用 JSDoc 注释：

```javascript
const client = require('./client')

/**
 * 查询 Xxx 列表
 * @param {object} params - { page, size, ... }
 */
function list(params) { return client.get('/xxx', params) }

/**
 * 查询 Xxx 详情
 * @param {number} id
 */
function detail(id) { return client.get('/xxx/' + id) }

/**
 * 创建 Xxx
 * @param {object} payload
 */
function create(payload) { return client.post('/xxx', payload) }

/**
 * 更新 Xxx
 * @param {number} id
 * @param {object} payload
 */
function update(id, payload) { return client.put('/xxx/' + id, payload) }

/**
 * 删除 Xxx
 * @param {number} id
 */
function del(id) { return client.del('/xxx/' + id) }

module.exports = { list, detail, create, update, del }
```

**命名约定：** 删除函数统一命名为 `del`（避免 `delete` 关键字冲突）。

---

## 页面规范

### 页面文件结构

每个页面包含 4 个文件，命名一致：

```
pages/xxx/list.js      # 页面逻辑 (Page)
pages/xxx/list.wxml    # 页面模板
pages/xxx/list.wxss    # 页面样式 (scoped by page class)
pages/xxx/list.json    # 页面配置 (title/background)
```

### JS 页面模式

```javascript
const auth = require('../../utils/auth')
const xxxApi = require('../../api/xxx')

const PAGE_SIZE = 20

Page({
  data: {
    // 状态变量用 ref/value 风格命名
    items: [],
    loading: false,
    hasMore: true,
    page: 1,
    refreshing: false,
    needRefresh: false      // 从子页面返回时标记需刷新
  },

  onLoad() {
    // 1. 检查登录
    if (!auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 2. 权限检查
    this.setData({ canCreate: auth.hasPermission('xxx:create') })
    // 3. 加载数据
    this.loadItems(true)
  },

  onShow() {
    // 从子页面返回时检查刷新标记
    if (this.data.needRefresh) {
      this.setData({ needRefresh: false })
      this.loadItems(true)
    }
  },

  // ↓↓↓ 事件处理 (handle + 动词)
  handleItemTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/xxx/detail?id=' + id })
  },

  handleCreate() {
    wx.navigateTo({ url: '/pages/xxx/form' })
  },

  // ↓↓↓ 分页加载 (reset=true 刷新, reset=false 追加)
  async loadItems(reset) {
    if (this.data.loading) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })
    try {
      const items = await xxxApi.list({ page, size: PAGE_SIZE })
      if (reset) {
        this.setData({ items, page: page + 1, hasMore: items.length >= PAGE_SIZE })
      } else {
        this.setData({
          items: this.data.items.concat(items),
          page: page + 1,
          hasMore: items.length >= PAGE_SIZE
        })
      }
    } catch {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false, refreshing: false })
    }
  },

  // ↓↓↓ 下拉刷新
  onPullDownRefresh() {
    this.setData({ refreshing: true })
    this.loadItems(true)
    wx.stopPullDownRefresh()
  },

  // ↓↓↓ 上拉加载
  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return
    this.loadItems(false)
  }
})
```

### 命名约定

| 类型 | 规则 | 示例 |
|------|------|------|
| 事件处理 | `handle + 名词 + 动词` | `handleCreate`, `handleItemTap`, `handleApprove` |
| 数据加载 | `load + 名词` | `loadItems`, `loadHistory`, `loadProjects` |
| 输入/选择变更 | `on + 字段 + Change` | `onStatusChange`, `onProjectChange` |
| 输入事件 (input) | `on + 字段 + Input` | `onCommentInput`, `onWasteTypeInput` |
| 格式化函数 | `format + 名词` (页面级工具函数) | `formatDate`, `formatDateTime` |
| 常量 | `UPPER_SNAKE_CASE` | `PAGE_SIZE`, `LOCATION_RISK` |

### 刷新回传模式

子页面修改数据后，通过上一页的 `needRefresh` 标记通知刷新：

```javascript
// 子页面中
notifyRefreshAndBack() {
  const pages = getCurrentPages()
  const prevPage = pages[pages.length - 2]
  if (prevPage) {
    prevPage.setData({ needRefresh: true })
  }
  setTimeout(() => { wx.navigateBack() }, 1000)
}
```

### JSON 页面配置

```json
{
  "usingComponents": {},
  "navigationBarTitleText": "页面标题",
  "navigationBarBackgroundColor": "#ffffff",
  "backgroundColor": "#f3f4f6"
}
```

---

## 认证 & 权限规范

### utils/auth.js — 认证状态管理

- **双重存储**：内存变量 (`_token`/`_user`) + wx.storage（防止小程序引擎回收模块变量）
- **登录后设置**：`auth.setAuth(token, user)` → 同时写内存和 storage
- **页面守卫**：每个 `onLoad()` 首行检查 `auth.isLoggedIn()`
- **权限判断**：通过 `user.permissions` 数组，`auth.hasPermission('xxx:create')`
- **登出**：`auth.logout()` → 清除内存 + storage → `wx.reLaunch('/pages/login/login')`

```javascript
const auth = require('../../utils/auth')

// 页面守卫模式
onLoad() {
  if (!auth.isLoggedIn()) {
    wx.reLaunch({ url: '/pages/login/login' })
    return
  }
}

// 权限检查
const user = auth.getUser()
const displayName = user.name || user.username  // name 优先，username 降级
this.setData({ canCreate: auth.hasPermission('waste:create') })
```

### 用户展示统一降级策略

```javascript
// 展示用户姓名：优先 name，降级 username
const displayName = user ? (user.name || user.username) : '未知'
```

---

## 样式规范 (WXSS)

### 全局样式 (app.wxss)

只设置 page 级别的基础样式：

```css
page {
  background-color: #f3f4f6;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 28rpx;
  color: #1f2937;
}
```

### 页面样式约定

- **页面级 class 前缀**：页面根元素以 `页面名-page` 命名
- **尺寸单位**：统一使用 `rpx`
- **颜色常量**：

| 用途 | 颜色值 |
|------|--------|
| 背景色 | `#f3f4f6` |
| 卡片/白色背景 | `#ffffff` |
| 主色 (Indigo) | `#6366f1` |
| 主色浅底 | `#eef2ff` |
| 主文字 | `#1f2937` |
| 次要文字 | `#6b7280` |
| 辅助文字 | `#9ca3af` |
| 成功/通过 | `#16a34a` / `#dcfce7` |
| 危险/驳回 | `#dc2626` / `#fecaca` |
| 警告/待处理 | `#d97706` / `#fef3c7` |
| 信息/进行中 | `#2563eb` / `#dbeafe` |

- **常用圆角**：`16rpx`（卡片）、`20rpx`（大卡片）、`10rpx`（小元素）、`8rpx`（按钮/标签）
- **常用阴影**：`0 1rpx 3rpx rgba(0,0,0,0.05)`（轻）、`0 1rpx 4rpx rgba(0,0,0,0.04)`（卡片）

### 布局模式

- 页面布局：`min-height: 100vh; background: #f3f4f6; padding-bottom: 40rpx;`
- 卡片列表：`.card-body { padding: 24rpx; }` + 底部操作栏 `.card-actions { border-top: 1rpx solid #f3f4f6; }`
- 空状态：`.state-box { display: flex; flex-direction: column; align-items: center; padding: 180rpx 0; }`
- 加载更多：`.load-more { text-align: center; padding: 30rpx 0; font-size: 24rpx; color: #d1d5db; }`

---

## 数据格式约定

### 日期处理

```javascript
// API 返回 ISO 字符串 → 截取为 YYYY-MM-DD
function formatDate(d) {
  if (!d) return ''
  if (typeof d === 'string' && d.length >= 10) return d.substring(0, 10)
  return d
}

// 日期时间 → YYYY-MM-DD HH:mm
function formatDateTime(dt) {
  if (!dt) return ''
  if (typeof dt === 'string') return dt.substring(0, 16).replace('T', ' ')
  return dt
}
```

### 枚举映射

```javascript
// 后端数字 code → 前端中文标签
const STATUSES = ['全部状态', '采购中', '已采购', '已到货']
const statusLabel = STATUSES[record.statusCode + 1] || '未知'
```

### 表单提交

```javascript
// POST/PUT payload 直接透传，可选字段用 undefined 过滤
const payload = {
  name: form.name.trim(),
  remark: form.remark.trim() || undefined,
  plannedStartDate: form.plannedStartDate  // 日期 picker value 直接传
}
```

---

## 入口规范 (app.js / app.json)

### app.js

```javascript
const auth = require('./utils/auth')

App({
  onLaunch() {
    // 已登录直接进 Dashboard，未登录留在 login 页（默认首页）
    if (auth.isLoggedIn()) {
      wx.reLaunch({ url: '/pages/dashboard/dashboard' })
    }
  },
  globalData: { userInfo: null }
})
```

### app.json

- `pages` 数组第一个为首页（`login`），上线后考虑改为 `dashboard`
- `window` 全局导航栏配置
- `permission` 声明位置权限用途说明
- `requiredPrivateInfos` 声明使用的地理位置接口

---

## 页面间通信

| 方式 | 场景 |
|------|------|
| URL query string | 传递简单 ID/参数：`wx.navigateTo({ url: '/pages/xxx/detail?id=123' })` |
| `needRefresh` 标记 | 子页面通知父页面刷新列表 |
| `getCurrentPages()` | 获取页面栈，直接调用上一页方法或 setData |

---

## 错误处理

- **网络错误**：统一 toast `'网络连接失败，请检查网络'`
- **业务错误**：toast `e.message`（来自后端 `msg` 字段）
- **空数据**：页面级 empty state（图标 + 提示文字），不弹 toast
- **加载中**：`loading` 状态 → WXML 条件渲染占位文本
