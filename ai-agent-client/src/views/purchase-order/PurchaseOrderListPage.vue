<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Table, Button, Select, Space, Modal, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'

const router = useRouter()
const store = usePurchaseOrderStore()

const filters = reactive({ status: undefined })
const pagination = reactive({ current: 1, pageSize: 20 })

const columns = [
  { title: '关联申请', dataIndex: 'purchaseRequests', key: 'purchaseRequests', width: 280 },
  { title: '供应商', dataIndex: 'supplierName', key: 'supplierName' },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '采购日期', dataIndex: 'purchaseDate', key: 'purchaseDate' },
  { title: '状态', dataIndex: 'statusCode', key: 'statusCode' },
  { title: '操作', key: 'action', width: 200 },
]

function formatPRNames(prs) {
  if (!prs || prs.length === 0) return '-'
  return prs.map(p => p.code || p.name || '-').join('、')
}

onMounted(() => loadData())

async function loadData() {
  try { await store.fetchList({ ...filters, page: pagination.current, size: pagination.pageSize }) }
  catch { /* store.error */ }
}

function handleView(id) { router.push(`/purchase-orders/${id}`) }
function handleEdit(id) { router.push(`/purchase-orders/${id}/edit`) }

async function handleDelete(record) {
  Modal.confirm({
    title: '确认删除', content: `确定要删除「${record.purchaseRequestName}」的采购记录吗？`,
    okText: '确认删除', okType: 'danger', cancelText: '取消',
    onOk: async () => { try { await store.remove(record.id); message.success('已删除'); loadData() } catch { message.error(store.error || '删除失败') } },
  })
}

function formatDate(d) { return d ? String(d).substring(0, 10) : '' }
</script>

<template>
  <div class="po-list-page">
    <div class="list-header">
      <h2>采购管理</h2>
      <Button type="primary" @click="router.push('/purchase-orders/new')"><PlusOutlined /> 新建采购记录</Button>
    </div>
    <Space style="margin-bottom:16px">
      <Select v-model:value="filters.status" placeholder="状态筛选" allowClear style="width:160px" @change="loadData"
        :options="[{value:0,label:'采购中'},{value:1,label:'已采购'},{value:2,label:'已到货'}]" />
    </Space>
    <Table :columns="columns" :data-source="store.items" :loading="store.loading" row-key="id"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: store.total, showSizeChanger: false }"
      @change="({ current }) => { pagination.current = current; loadData() }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'purchaseRequests'">{{ formatPRNames(record.purchaseRequests) }}</template>
        <template v-if="column.key === 'purchaseDate'">{{ formatDate(record.purchaseDate) }}</template>
        <template v-if="column.key === 'statusCode'">{{ store.getStatusLabel(record.statusCode) }}</template>
        <template v-if="column.key === 'action'">
          <Space>
            <Button type="link" size="small" @click="handleView(record.id)">查看</Button>
            <Button v-if="record.statusCode === 0" type="link" size="small" @click="handleEdit(record.id)">编辑</Button>
            <Button v-if="record.statusCode === 0" type="link" size="small" danger @click="handleDelete(record)">删除</Button>
          </Space>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.po-list-page { padding: 24px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.list-header h2 { margin: 0; font-size: 20px; }
</style>
