<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConstructionWasteStore } from '@/stores/constructionWaste'
import { useAuthStore } from '@/stores/auth'
import client from '@/api/client'
import { Table, Button, Select, Input, DatePicker, Space, Tag, Modal, message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons-vue'

const store = useConstructionWasteStore()
const auth = useAuthStore()
const router = useRouter()

const projects = ref([])
const filters = reactive({ projectId: undefined, wasteType: undefined, startDate: undefined, endDate: undefined })

const columns = [
  { title: '项目', key: 'projectName', width: 160 },
  { title: '废渣种类', dataIndex: 'wasteType', width: 120 },
  { title: '数量', key: 'quantity', width: 100 },
  { title: '运出日期', key: 'removalDate', width: 120 },
  { title: '司机', dataIndex: 'driverName', width: 80 },
  { title: '车牌号', dataIndex: 'vehiclePlate', width: 110 },
  { title: '处置去向', dataIndex: 'destination', width: 160, ellipsis: true },
  { title: '操作', key: 'action', width: 120 },
]

onMounted(async () => {
  store.fetchAll()
  try {
    const res = await client.get('/projects/select')
    projects.value = res.data
  } catch { /* ignore */ }
})

function handleSearch() {
  store.fetchAll(filters)
}

function handleView(id) {
  router.push(`/construction-wastes/${id}`)
}

async function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除「${record.wasteType}」的运出记录吗？关联的照片将一并删除。`,
    okText: '确认删除',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        await store.remove(record.id)
        message.success('已删除')
        store.fetchAll(filters)
      } catch {
        message.error(store.error || '删除失败')
      }
    },
  })
}

function formatDate(d) {
  if (!d) return ''
  return String(d).substring(0, 10)
}

function projName(id) {
  return projects.value.find(p => p.id === id)?.name || '未知'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>建筑废渣管理</h2>
      <Button v-if="auth.hasPermission('waste:create')" type="primary" @click="router.push('/construction-wastes/new')">
        <PlusOutlined /> 新建运出记录
      </Button>
    </div>

    <Space style="margin-bottom:16px" wrap>
      <Select
        v-model:value="filters.projectId"
        placeholder="选择项目"
        allowClear
        style="width:200px"
        @change="handleSearch"
        :options="projects.map(p => ({ value: p.id, label: p.name }))"
      />
      <Input v-model:value="filters.wasteType" placeholder="废渣种类" allowClear style="width:160px" @change="handleSearch" />
      <DatePicker v-model:value="filters.startDate" placeholder="开始日期" style="width:140px" @change="handleSearch" />
      <DatePicker v-model:value="filters.endDate" placeholder="结束日期" style="width:140px" @change="handleSearch" />
    </Space>

    <Table :columns="columns" :data-source="store.records" :loading="store.loading" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'projectName'">{{ projName(record.projectId) }}</template>
        <template v-if="column.key === 'quantity'">{{ record.quantity }} {{ record.unit }}</template>
        <template v-if="column.key === 'removalDate'">{{ formatDate(record.removalDate) }}</template>
        <template v-if="column.key === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleView(record.id)"><EyeOutlined /> 详情</Button>
            <Button v-if="auth.hasPermission('waste:delete')" type="link" danger size="small" @click="handleDelete(record)"><DeleteOutlined /> 删除</Button>
          </Space>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.page-container { background:#fff;padding:24px;border-radius:8px }
.page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:20px }
.page-header h2 { margin:0;font-size:18px }
</style>
