<script setup>
import { ref, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApprovalStore } from '@/stores/approval'
import { useRouter, useRoute } from 'vue-router'
import { UserOutlined, LogoutOutlined, DashboardOutlined, ProjectOutlined, ShoppingOutlined, SettingOutlined, CheckSquareOutlined, TeamOutlined, SafetyOutlined, HomeOutlined, DeleteOutlined } from '@ant-design/icons-vue'

const auth = useAuthStore()
const approval = useApprovalStore()
const router = useRouter()
const route = useRoute()

onMounted(() => {
  approval.fetchTasks({ size: 1 }).catch(() => {})
})

const selectedKeys = ref(['dashboard'])

watch(() => route.path, (path) => {
  if (path.startsWith('/projects')) {
    selectedKeys.value = ['projects']
  } else if (path.startsWith('/procurement')) {
    selectedKeys.value = ['procurement']
  } else if (path.startsWith('/tasks')) {
    selectedKeys.value = ['tasks']
  } else if (path.startsWith('/users')) {
    selectedKeys.value = ['users']
  } else if (path.startsWith('/roles')) {
    selectedKeys.value = ['roles']
  } else if (path.startsWith('/warehouses')) {
    selectedKeys.value = ['warehouses']
  } else if (path.startsWith('/materials') || path.startsWith('/stock-records')) {
    selectedKeys.value = ['warehouses']
  } else if (path.startsWith('/settings')) {
    selectedKeys.value = ['settings']
  } else if (path.startsWith('/construction-wastes')) {
    selectedKeys.value = ['construction-wastes']
  } else if (path.startsWith('/purchase-orders')) {
    selectedKeys.value = ['purchase-orders']
  } else if (path === '/dashboard') {
    selectedKeys.value = ['dashboard']
  }
}, { immediate: true })

function handleMenuClick({ key }) {
  if (key === 'logout') {
    handleLogout()
  } else if (key === 'projects') {
    router.push('/projects')
  } else if (key === 'procurement') {
    router.push('/procurement')
  } else if (key === 'tasks') {
    router.push('/tasks')
  } else if (key === 'users') {
    router.push('/users')
  } else if (key === 'roles') {
    router.push('/roles')
  } else if (key === 'warehouses') {
    router.push('/warehouses')
  } else if (key === 'warehouses-list') {
    router.push('/warehouses')
  } else if (key === 'materials-list') {
    router.push('/materials')
  } else if (key === 'stock-records-list') {
    router.push('/stock-records')
  } else if (key === 'settings') {
    router.push('/settings')
  } else if (key === 'construction-wastes') {
    router.push('/construction-wastes')
  } else if (key === 'purchase-orders') {
    router.push('/purchase-orders')
  } else if (key === 'dashboard') {
    router.push('/dashboard')
  }
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout class="dashboard-layout">
    <a-layout-header class="dashboard-header">
      <div class="header-left">
        <div class="logo">
          <div class="logo-icon">
            <span class="logo-emoji">🏗️</span>
          </div>
          <span class="logo-text">智能管理平台</span>
        </div>
        <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="horizontal"
            class="header-menu"
            @click="handleMenuClick"
        >
          <a-menu-item key="dashboard">
            <DashboardOutlined />
            <span>Dashboard</span>
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('project:read')" key="projects">
            <ProjectOutlined />
            <span>项目管理</span>
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('pr:read')" key="procurement">
            <ShoppingOutlined />
            <span>采购申请</span>
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('task:read')" key="tasks">
            <CheckSquareOutlined />
            <span>待办任务</span>
            <a-badge v-if="approval.total > 0" :count="approval.total" :overflow-count="99" style="margin-left: 4px" />
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('user:read')" key="users">
            <TeamOutlined />
            <span>用户管理</span>
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('role:read')" key="roles">
            <SafetyOutlined />
            <span>角色管理</span>
          </a-menu-item>
          <a-sub-menu v-if="auth.hasPermission('warehouse:read')" key="warehouses">
            <template #title><HomeOutlined /><span>仓库管理</span></template>
            <a-menu-item key="warehouses-list">仓库信息</a-menu-item>
            <a-menu-item key="materials-list">物料管理</a-menu-item>
            <a-menu-item key="stock-records-list">出入库记录</a-menu-item>
          </a-sub-menu>
          <a-menu-item v-if="auth.hasPermission('waste:read')" key="construction-wastes">
            <DeleteOutlined />
            <span>建筑废渣</span>
          </a-menu-item>
          <a-menu-item v-if="auth.hasPermission('purchase:read')" key="purchase-orders">
            <ShoppingOutlined />
            <span>采购管理</span>
          </a-menu-item>
          <a-menu-item key="settings">
            <SettingOutlined />
            <span>设置</span>
          </a-menu-item>
        </a-menu>
      </div>
      <div class="header-right">
        <a-dropdown>
          <div class="user-dropdown">
            <a-avatar size="small">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <span class="username">{{ auth.user?.name || auth.user?.username || '用户' }}</span>
          </div>
          <template #overlay>
            <a-menu @click="handleMenuClick">
              <a-menu-item key="logout">
                <LogoutOutlined />
                <span style="margin-left: 8px">退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    <a-layout-content class="dashboard-content">
      <router-view />
    </a-layout-content>
  </a-layout>
</template>

<style scoped>
.dashboard-layout {
  width: 100%;
  min-height: 100vh;
  background: #f3f4f6;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 28px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  line-height: 60px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: 40px;
}

.logo-icon {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(99,102,241,0.25);
}

.logo-emoji { font-size: 18px; }

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.header-menu {
  border-bottom: none;
  line-height: 60px;
}

.header-menu :deep(.ant-menu-item-selected) {
  color: #6366f1 !important;
}
.header-menu :deep(.ant-menu-item-selected::after) {
  border-bottom-color: #6366f1 !important;
}
.header-menu :deep(.ant-menu-item:hover) {
  color: #6366f1 !important;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f3f4f6;
}

.username {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.dashboard-content {
  padding: 20px 24px;
}
</style>
