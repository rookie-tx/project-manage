<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Table,
  Button,
  Select,
  Space,
  Modal,
  message,
} from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'
import SubmitApprovalDialog from '@/components/approval/SubmitApprovalDialog.vue'
import client from '@/api/client'

const router = useRouter()
const route = useRoute()
const store = usePurchaseRequestStore()

const projectOptions = ref([])
const searchProjectId = ref(null)
const submitDialogVisible = ref(false)
const submitPrId = ref(null)

const pagination = reactive({
  current: 1,
  pageSize: 20,
})

function initFromQuery() {
  const q = route.query
  if (q.projectId) searchProjectId.value = Number(q.projectId)
  if (q.page) pagination.current = Number(q.page)
}

function syncQuery() {
  const q = {}
  if (searchProjectId.value) q.projectId = searchProjectId.value
  if (pagination.current > 1) q.page = pagination.current
  router.replace({ query: q })
}

async function loadData() {
  const params = { page: pagination.current, size: pagination.pageSize }
  if (searchProjectId.value) params.projectId = searchProjectId.value
  try {
    await store.fetchList(params)
  } catch {
    // error is in store
  }
}

async function loadProjectOptions() {
  try {
    const { data } = await client.get('/projects/select')
    projectOptions.value = data.map((p) => ({
      value: p.id,
      label: p.code + ' ' + p.name,
    }))
  } catch {
    // ignore
  }
}

function handleSearch() {
  pagination.current = 1
  syncQuery()
  loadData()
}

function handlePageChange(page) {
  pagination.current = page
  syncQuery()
  loadData()
}

function handleView(id) {
  router.push(`/procurement/${id}`)
}

function handleEdit(id) {
  router.push(`/procurement/${id}/edit`)
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据不可恢复，确定要删除此采购申请吗？',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await store.remove(id)
        message.success('删除成功')
        await loadData()
      } catch {
        message.error(store.error || '删除失败')
      }
    },
  })
}

function handleSubmitApproval(id) {
  submitPrId.value = id
  submitDialogVisible.value = true
}

async function handleSubmitDialogClose(refresh) {
  submitDialogVisible.value = false
  submitPrId.value = null
  if (refresh) {
    await loadData()
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr
}

const columns = [
  { title: '编号', dataIndex: 'code', key: 'code', width: 140 },
  { title: '项目', dataIndex: 'projectName', key: 'projectName' },
  { title: '物料名称', dataIndex: 'name', key: 'name' },
  { title: '规格', dataIndex: 'specification', key: 'specification' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '单位', dataIndex: 'unitCode', key: 'unitCode' },
  { title: '到场时间', dataIndex: 'arrivalDate', key: 'arrivalDate' },
  { title: '状态', dataIndex: 'statusCode', key: 'statusCode' },
  { title: '操作', key: 'action', width: 200 },
]

onMounted(() => {
  initFromQuery()
  loadProjectOptions()
  loadData()
})
</script>

<template>
  <div class="pr-list-page">
    <div class="list-header">
      <h2>采购申请</h2>
      <Button type="primary" @click="router.push('/procurement/new')">
        <PlusOutlined />
        新建申请
      </Button>
    </div>

    <div class="list-filters">
      <Space>
        <Select
          v-model:value="searchProjectId"
          placeholder="按项目筛选"
          allow-clear
          show-search
          option-filter-prop="label"
          style="width: 240px"
          :options="projectOptions"
        />
        <Button type="primary" @click="handleSearch">
          <SearchOutlined />
          搜索
        </Button>
      </Space>
    </div>

    <Table
      :columns="columns"
      :data-source="store.items"
      :loading="store.loading"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: store.total,
        showSizeChanger: false,
      }"
      row-key="id"
      @change="({ current }) => handlePageChange(current)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'quantity'">
          {{ record.quantity }}
        </template>
        <template v-else-if="column.key === 'unitCode'">
          {{ store.getUnitLabel(record.unitCode) }}
        </template>
        <template v-else-if="column.key === 'arrivalDate'">
          {{ formatDate(record.arrivalDate) }}
        </template>
        <template v-else-if="column.key === 'statusCode'">
          {{ store.getStatusLabel(record.statusCode) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleView(record.id)">查看</Button>
            <template v-if="record.statusCode === 0">
              <Button type="link" size="small" @click="handleSubmitApproval(record.id)">提交审批</Button>
              <Button type="link" size="small" @click="handleEdit(record.id)">编辑</Button>
              <Button type="link" size="small" danger @click="handleDelete(record.id)">删除</Button>
            </template>
          </Space>
        </template>
      </template>
    </Table>

    <SubmitApprovalDialog
      v-if="submitDialogVisible"
      :visible="submitDialogVisible"
      :purchase-request-id="submitPrId"
      @close="handleSubmitDialogClose"
    />
  </div>
</template>

<style scoped>
.pr-list-page {
  padding: 24px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list-header h2 {
  margin: 0;
  font-size: 20px;
}

.list-filters {
  margin-bottom: 16px;
}
</style>
