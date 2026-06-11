<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Modal,
  message,
} from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()

const searchForm = reactive({
  keyword: '',
  type: null,
  status: null,
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
})

function initFromQuery() {
  const q = route.query
  if (q.keyword) searchForm.keyword = q.keyword
  if (q.type) searchForm.type = Number(q.type)
  if (q.status) searchForm.status = Number(q.status)
  if (q.page) pagination.current = Number(q.page)
}

function syncQuery() {
  const q = {}
  if (searchForm.keyword) q.keyword = searchForm.keyword
  if (searchForm.type !== null && searchForm.type !== undefined) q.type = searchForm.type
  if (searchForm.status !== null && searchForm.status !== undefined) q.status = searchForm.status
  if (pagination.current > 1) q.page = pagination.current
  router.replace({ query: q })
}

async function loadData() {
  const params = { page: pagination.current, size: pagination.pageSize }
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.type !== null && searchForm.type !== undefined) params.type = searchForm.type
  if (searchForm.status !== null && searchForm.status !== undefined) params.status = searchForm.status
  try {
    await projectStore.fetchList(params)
  } catch {
    // error is in store
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
  router.push(`/projects/${id}`)
}

function handleEdit(id) {
  router.push(`/projects/${id}/edit`)
}

function handleDelete(id) {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据不可恢复，确定要删除此项目吗？',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await projectStore.remove(id)
        message.success('删除成功')
        await loadData()
      } catch {
        message.error(projectStore.error || '删除失败')
      }
    },
  })
}

const columns = [
  { title: '项目编号', dataIndex: 'code', key: 'code' },
  { title: '项目名称', dataIndex: 'name', key: 'name' },
  { title: '项目类型', dataIndex: 'type', key: 'type' },
  { title: '项目状态', dataIndex: 'status', key: 'status' },
  { title: '甲方名称', dataIndex: 'clientName', key: 'clientName' },
  { title: '项目经理', dataIndex: 'projectManagerName', key: 'projectManagerName' },
  { title: '操作', key: 'action', width: 200 },
]

function getTypeLabel(code) {
  return projectStore.getTypeLabel(code)
}

function getStatusLabel(code) {
  return projectStore.getStatusLabel(code)
}

onMounted(() => {
  initFromQuery()
  loadData()
})
</script>

<template>
  <div class="project-list-page">
    <div class="list-header">
      <h2>项目管理</h2>
      <Button type="primary" @click="router.push('/projects/new')">
        <PlusOutlined />
        新建项目
      </Button>
    </div>

    <div class="list-filters">
      <Space>
        <Input
          v-model:value="searchForm.keyword"
          placeholder="搜索项目名称"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        />
        <Select
          v-model:value="searchForm.type"
          placeholder="项目类型"
          allow-clear
          style="width: 140px"
          :options="projectStore.getTypeOptions()"
        />
        <Select
          v-model:value="searchForm.status"
          placeholder="项目状态"
          allow-clear
          style="width: 140px"
          :options="projectStore.getStatusOptions()"
        />
        <Button type="primary" @click="handleSearch">
          <SearchOutlined />
          搜索
        </Button>
      </Space>
    </div>

    <Table
      :columns="columns"
      :data-source="projectStore.items"
      :loading="projectStore.loading"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: projectStore.total,
        showSizeChanger: false,
      }"
      row-key="id"
      @change="({ current }) => handlePageChange(current)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          {{ getTypeLabel(record.type) }}
        </template>
        <template v-else-if="column.key === 'status'">
          {{ getStatusLabel(record.status) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleView(record.id)">查看</Button>
            <Button type="link" size="small" @click="handleEdit(record.id)">编辑</Button>
            <Button type="link" size="small" danger @click="handleDelete(record.id)">删除</Button>
          </Space>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.project-list-page {
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
