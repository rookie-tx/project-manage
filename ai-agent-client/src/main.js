import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// Ant Design 全局主题 — 对齐 indigo 设计系统
const token = document.createElement('style')
token.textContent = `
  :root {
    --ant-primary-color: #6366f1;
    --ant-primary-color-hover: #4f46e5;
    --ant-primary-color-active: #4338ca;
    --ant-primary-1: #eef2ff;
    --ant-primary-2: #c7d2fe;
    --ant-primary-5: #6366f1;
    --ant-primary-6: #4f46e5;
    --ant-primary-7: #4338ca;
    --ant-info-color: #6366f1;
    --ant-success-color: #16a34a;
    --ant-warning-color: #d97706;
    --ant-error-color: #ef4444;
  }
  .ant-btn-primary { background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%); border-color: #6366f1; box-shadow: 0 2px 6px rgba(99,102,241,0.25); }
  .ant-btn-primary:hover { background: #4f46e5; border-color: #4f46e5; }
  .ant-tag { border-radius: 6px; }
  .ant-card { border-radius: 16px; }
  .ant-table { border-radius: 12px; }
  .ant-pagination .ant-pagination-item-active { border-color: #6366f1; }
  .ant-pagination .ant-pagination-item-active a { color: #6366f1; }
  .ant-spin-dot-item { background-color: #6366f1; }
`
document.head.appendChild(token)

app.mount('#app')
