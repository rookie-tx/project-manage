<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Table, Button } from 'ant-design-vue'
import { useApprovalStore } from '@/stores/approval'
import ApprovalDialog from '@/components/approval/ApprovalDialog.vue'

const router = useRouter()
const store = useApprovalStore()

const businessDetailRoutes = {
  PURCHASE_REQUEST: (id) => `/procurement/${id}`,
}

function goToDetail(record) {
  const pathFn = businessDetailRoutes[record.businessType]
  if (pathFn) {
    router.push(pathFn(record.businessId))
  }
}
const dialogVisible = ref(false)
const selectedTask = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 20,
})

async function loadData() {
  try {
    await store.fetchTasks({ page: pagination.current, size: pagination.pageSize })
  } catch {
    // error is in store
  }
}

function handleRowClick(record) {
  selectedTask.value = record
  dialogVisible.value = true
}

function handlePageChange(page) {
  pagination.current = page
  loadData()
}

async function handleDialogClose(refresh) {
  dialogVisible.value = false
  selectedTask.value = null
  if (refresh) {
    await loadData()
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr
}

const columns = [
  { title: '业务标题', dataIndex: 'businessTitle', key: 'businessTitle' },
  { title: '业务副标题', dataIndex: 'businessSubtitle', key: 'businessSubtitle' },
  { title: '当前节点', dataIndex: 'nodeName', key: 'nodeName' },
  { title: '到达时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 180 },
]

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="task-list-page">
    <h2>待办任务</h2>

    <Table
      :columns="columns"
      :data-source="store.tasks"
      :loading="store.loading"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: store.total,
        showSizeChanger: false,
      }"
      row-key="taskId"
      @change="({ current }) => handlePageChange(current)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'createTime'">
          {{ formatDate(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goToDetail(record)">查看详情</a-button>
            <Button type="primary" size="small" @click="handleRowClick(record)">处理</Button>
          </a-space>
        </template>
      </template>
    </Table>

    <ApprovalDialog
      v-if="dialogVisible"
      :visible="dialogVisible"
      :task="selectedTask"
      @close="handleDialogClose"
    />
  </div>
</template>

<style scoped>
.task-list-page {
  padding: 24px;
}

.task-list-page h2 {
  margin: 0 0 16px;
  font-size: 20px;
}
</style>
