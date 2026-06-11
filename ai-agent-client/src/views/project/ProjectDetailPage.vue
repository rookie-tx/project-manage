<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'
import { Descriptions, Button, Space, Spin, Modal, Tabs, TabPane, message } from 'ant-design-vue'
import { useProjectStore } from '@/stores/project'
import TempWorkerTab from './TempWorkerTab.vue'
import AttendanceAreaTab from '@/views/attendance/AttendanceAreaTab.vue'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()

onMounted(async () => {
  try {
    await projectStore.fetchDetail(route.params.id)
  } catch {
    // error is in store
  }
})

function handleEdit() {
  router.push(`/projects/${route.params.id}/edit`)
}

function handleDelete() {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据不可恢复，确定要删除此项目吗？',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await projectStore.remove(route.params.id)
        message.success('删除成功')
        router.push('/projects')
      } catch {
        message.error(projectStore.error || '删除失败')
      }
    },
  })
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr
}
</script>

<template>
  <div class="project-detail-page">
    <Spin :spinning="projectStore.loading">
      <template v-if="projectStore.error">
        <p class="detail-error">{{ projectStore.error }}</p>
        <Button @click="router.push('/projects')">返回列表</Button>
      </template>

      <template v-else-if="projectStore.current">
        <div class="detail-header">
          <h2>{{ projectStore.current.name }}</h2>
          <Space>
            <Button @click="handleEdit">编辑</Button>
            <Button danger @click="handleDelete">删除</Button>
            <Button @click="router.push('/projects')">返回列表</Button>
          </Space>
        </div>

        <Tabs default-active-key="info" style="margin-top: 16px">
          <TabPane key="info" tab="基本信息">
            <Descriptions bordered :column="2">
              <Descriptions.Item label="项目编号">{{ projectStore.current.code }}</Descriptions.Item>
              <Descriptions.Item label="项目名称">{{ projectStore.current.name }}</Descriptions.Item>
              <Descriptions.Item label="项目类型">
                {{ projectStore.getTypeLabel(projectStore.current.type) }}
              </Descriptions.Item>
              <Descriptions.Item label="项目状态">
                {{ projectStore.getStatusLabel(projectStore.current.status) }}
              </Descriptions.Item>
              <Descriptions.Item label="项目地点">{{ projectStore.current.location || '-' }}</Descriptions.Item>
              <Descriptions.Item label="甲方名称">{{ projectStore.current.clientName || '-' }}</Descriptions.Item>
              <Descriptions.Item label="监理单位">{{ projectStore.current.supervisorUnit || '-' }}</Descriptions.Item>
              <Descriptions.Item label="项目经理">{{ projectStore.current.projectManagerName || '-' }}</Descriptions.Item>
              <Descriptions.Item label="计划开工日期">{{ formatDate(projectStore.current.plannedStartDate) }}</Descriptions.Item>
              <Descriptions.Item label="计划竣工日期">{{ formatDate(projectStore.current.plannedEndDate) }}</Descriptions.Item>
              <Descriptions.Item label="实际开工日期">{{ formatDate(projectStore.current.actualStartDate) }}</Descriptions.Item>
              <Descriptions.Item label="实际竣工日期">{{ formatDate(projectStore.current.actualEndDate) }}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{{ formatDate(projectStore.current.createdAt) }}</Descriptions.Item>
              <Descriptions.Item label="更新时间">{{ formatDate(projectStore.current.updatedAt) }}</Descriptions.Item>
              <Descriptions.Item label="备注" :span="2">{{ projectStore.current.remark || '-' }}</Descriptions.Item>
            </Descriptions>
          </TabPane>
          <TabPane key="workers" tab="临时工">
            <TempWorkerTab :project-id="Number(route.params.id)" />
          </TabPane>
          <TabPane key="attendance-areas" tab="打卡区域">
            <AttendanceAreaTab :project-id="Number(route.params.id)" />
          </TabPane>
        </Tabs>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.project-detail-page {
  padding: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header h2 {
  margin: 0;
  font-size: 20px;
}

.detail-error {
  color: #ff4d4f;
  font-size: 16px;
  margin-bottom: 16px;
}
</style>
