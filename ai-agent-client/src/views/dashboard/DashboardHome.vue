<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApprovalStore } from '@/stores/approval'
import { Card, Row, Col, Statistic } from 'ant-design-vue'
import { ProjectOutlined, ShoppingOutlined, CheckSquareOutlined, DeleteOutlined } from '@ant-design/icons-vue'

const auth = useAuthStore()
const approval = useApprovalStore()

const greeting = ref('')

onMounted(() => {
  const h = new Date().getHours()
  if (h < 6) greeting.value = '夜深了'
  else if (h < 9) greeting.value = '早上好'
  else if (h < 12) greeting.value = '上午好'
  else if (h < 14) greeting.value = '中午好'
  else if (h < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'
  approval.fetchTasks({ size: 1 }).catch(() => {})
})

const shortcuts = [
  { title: '项目管理', icon: ProjectOutlined, path: '/projects', color: '#6366f1', bg: '#eef2ff', perm: 'project:read' },
  { title: '采购申请', icon: ShoppingOutlined, path: '/procurement', color: '#d97706', bg: '#fef3c7', perm: 'pr:read' },
  { title: '待办任务', icon: CheckSquareOutlined, path: '/tasks', color: '#16a34a', bg: '#dcfce7', perm: 'task:read', badge: true },
  { title: '建筑废渣', icon: DeleteOutlined, path: '/construction-wastes', color: '#8b5cf6', bg: '#ede9fe', perm: 'waste:read' },
].filter(s => !s.perm || auth.hasPermission(s.perm))
</script>

<template>
  <div class="dashboard-home">
    <h1 class="welcome">{{ greeting }}，{{ auth.user?.name || '用户' }}</h1>
    <p class="welcome-sub">欢迎使用智能管理平台</p>

    <Row :gutter="20" style="margin-top: 24px">
      <Col :span="6" v-for="s in shortcuts" :key="s.path">
        <Card hoverable class="shortcut-card" @click="$router.push(s.path)">
          <div class="shortcut-content">
            <div class="shortcut-icon" :style="{ background: s.bg, color: s.color }">
              <component :is="s.icon" />
            </div>
            <div class="shortcut-info">
              <span class="shortcut-title">{{ s.title }}</span>
              <span class="shortcut-desc">点击进入</span>
            </div>
          </div>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<style scoped>
.dashboard-home {
  max-width: 1200px;
}

.welcome {
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.welcome-sub {
  font-size: 15px;
  color: #9ca3af;
  margin-top: 6px;
}

.shortcut-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s, transform 0.15s;
}

.shortcut-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.shortcut-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.shortcut-icon {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
}

.shortcut-info {
  display: flex; flex-direction: column;
}

.shortcut-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.shortcut-desc {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 2px;
}
</style>
