## 1. API 层

- [x] 1.1 修改 `src/api/client.js` 响应拦截器：`code === 0` 时解包 `data`，`code !== 0` 时 `Promise.reject(new Error(msg))`

## 2. Store 适配

- [x] 2.1 修改 `src/stores/auth.js`：`fetchUser()` 适配解包后的响应数据
- [x] 2.2 修改 `src/stores/auth.js`：`login()` 移除硬编码错误文案，改用 catch 中的 `e.message`
