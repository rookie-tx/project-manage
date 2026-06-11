<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserManageStore } from '@/stores/userManage'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Input, Select, Tag, Alert, Modal, Space } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const store = useUserManageStore()
const auth = useAuthStore()

const keyword = ref('')
const statusFilter = ref(undefined)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '姓名', key: 'name', width: 100 },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '角色', dataIndex: 'roles', key: 'roles' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 160 },
]

onMounted(() => {
  store.fetchUsers()
})

function handleSearch() {
  store.search(keyword.value)
}

function handleStatusChange(value) {
  store.filterByStatus(value)
}

function handleCreate() {
  router.push('/users/new')
}

function handleEdit(id) {
  router.push(`/users/${id}/edit`)
}

function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除用户「${record.username}」？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      await store.deleteUser(record.id)
      store.fetchUsers()
    },
  })
}

function handleTableChange(pag) {
  if (pag.current !== store.searchParams.page) {
    store.setPage(pag.current)
  }
  if (pag.pageSize !== store.searchParams.size) {
    store.setSize(pag.pageSize)
  }
}

function getStatusTag(status) {
  if (status === 'ACTIVE') return { color: 'green', text: '正常' }
  return { color: 'default', text: '禁用' }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}
</script>

<template>
  <div class="user-list-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <Button v-if="auth.hasPermission('user:create')" type="primary" @click="handleCreate">
        <PlusOutlined />
        创建用户
      </Button>
    </div>

    <Alert v-if="store.error" type="error" :message="store.error" closable style="margin-bottom: 16px" />

    <div class="search-bar">
      <Space>
        <Input v-model:value="keyword" placeholder="搜索用户名" style="width: 200px" @press-enter="handleSearch">
          <template #suffix>
            <SearchOutlined @click="handleSearch" style="cursor: pointer; color: #999" />
          </template>
        </Input>
        <Select
          v-model:value="statusFilter"
          placeholder="状态筛选"
          style="width: 120px"
          allow-clear
          @change="handleStatusChange"
        >
          <Select.Option :value="1">正常</Select.Option>
          <Select.Option :value="0">禁用</Select.Option>
        </Select>
      </Space>
    </div>

    <Table
      :columns="columns"
      :data-source="store.records"
      :loading="store.loading"
      :pagination="{
        current: store.searchParams.page,
        pageSize: store.searchParams.size,
        total: store.total,
        showSizeChanger: true,
        showTotal: (total) => `共 ${total} 条`,
      }"
      row-key="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">{{ record.name || '-' }}</template>
        <template v-if="column.key === 'status'">
          <Tag :color="getStatusTag(record.status).color">{{ getStatusTag(record.status).text }}</Tag>
        </template>
        <template v-if="column.key === 'roles'">
          <Tag v-for="role in record.roles" :key="role" style="margin-right: 4px">{{ role }}</Tag>
        </template>
        <template v-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-if="column.key === 'action'">
          <Space>
            <a v-if="auth.hasPermission('user:update')" @click="handleEdit(record.id)">编辑</a>
            <a v-if="auth.hasPermission('user:delete')" style="color: #ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.user-list-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.search-bar {
  margin-bottom: 16px;
}
</style>
