<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStockRecordStore, useWarehouseStore, useMaterialStore } from '@/stores/warehouse'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Select, InputNumber, Input, Form, FormItem, Modal, Alert, Space, Tag, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const store = useStockRecordStore()
const whStore = useWarehouseStore()
const matStore = useMaterialStore()
const auth = useAuthStore()

const filters = reactive({ warehouseId: undefined, materialId: undefined, type: undefined })
const modalVisible = ref(false)
const submitting = ref(false)
const error = ref(null)
const formState = reactive({ warehouseId: null, materialId: null, type: 1, quantity: 1, purchaseRequestId: null, projectId: null })

const columns = [
  { title: '时间', dataIndex: 'createdAt', width: 170 },
  { title: '仓库', key: 'warehouse', width: 120 },
  { title: '物料', key: 'material', width: 120 },
  { title: '类型', key: 'type', width: 80 },
  { title: '数量', dataIndex: 'quantity', width: 80 },
  { title: '关联单据', key: 'ref', width: 140 },
  { title: '操作人', dataIndex: 'operatorId', width: 80 },
]

function whName(id) { return whStore.warehouses.find(w => w.id === id)?.name || '未知' }
function matName(id) { return matStore.materials.find(m => m.id === id)?.name || '未知' }
function formatDate(d) { if (!d) return ''; return d.replace('T', ' ').substring(0, 19) }

onMounted(() => { store.fetchAll(); whStore.fetchAll(); matStore.fetchAll() })

function handleSearch() { store.fetchAll(filters) }
function openStock(type) {
  formState.warehouseId = null; formState.materialId = null; formState.type = type; formState.quantity = 1
  formState.purchaseRequestId = null; formState.projectId = null; error.value = null; modalVisible.value = true
}
async function handleSubmit() {
  error.value = null; submitting.value = true
  try {
    const payload = { warehouseId: formState.warehouseId, materialId: formState.materialId, type: formState.type, quantity: formState.quantity }
    if (formState.purchaseRequestId) payload.purchaseRequestId = Number(formState.purchaseRequestId)
    if (formState.projectId) payload.projectId = Number(formState.projectId)
    await store.createStock(payload)
    modalVisible.value = false; store.fetchAll(filters); message.success('操作成功')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header"><h2>出入库管理</h2>
      <Space>
        <Button v-if="auth.hasPermission('stock:in')" type="primary" @click="openStock(1)"><PlusOutlined />入库</Button>
        <Button v-if="auth.hasPermission('stock:out')" @click="openStock(2)"><PlusOutlined />出库</Button>
      </Space>
    </div>

    <Space style="margin-bottom:16px">
      <Select v-model:value="filters.warehouseId" placeholder="仓库" allowClear style="width:150px" @change="handleSearch">
        <Select.Option v-for="w in whStore.warehouses" :key="w.id" :value="w.id">{{ w.name }}</Select.Option>
      </Select>
      <Select v-model:value="filters.materialId" placeholder="物料" allowClear style="width:150px" @change="handleSearch">
        <Select.Option v-for="m in matStore.materials" :key="m.id" :value="m.id">{{ m.name }}</Select.Option>
      </Select>
      <Select v-model:value="filters.type" placeholder="类型" allowClear style="width:100px" @change="handleSearch">
        <Select.Option :value="1">入库</Select.Option>
        <Select.Option :value="2">出库</Select.Option>
      </Select>
    </Space>

    <Table :columns="columns" :data-source="store.records" :loading="store.loading" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</template>
        <template v-if="column.key === 'warehouse'">{{ whName(record.warehouseId) }}</template>
        <template v-if="column.key === 'material'">{{ matName(record.materialId) }}</template>
        <template v-if="column.key === 'type'">
          <Tag :color="record.type === 'IN' ? 'green' : 'red'">{{ record.type === 'IN' ? '入库' : '出库' }}</Tag>
        </template>
        <template v-if="column.key === 'ref'">
          <template v-if="record.purchaseRequestId">采购单 #{{ record.purchaseRequestId }}</template>
          <template v-else-if="record.projectId">项目 #{{ record.projectId }}</template>
          <span v-else style="color:#999">-</span>
        </template>
      </template>
    </Table>

    <Modal v-model:open="modalVisible" :title="formState.type===1?'入库':'出库'" :confirm-loading="submitting" @ok="handleSubmit">
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
      <Form layout="vertical">
        <FormItem label="仓库" required><Select v-model:value="formState.warehouseId" placeholder="选择仓库"><Select.Option v-for="w in whStore.warehouses" :key="w.id" :value="w.id">{{ w.name }}</Select.Option></Select></FormItem>
        <FormItem label="物料" required><Select v-model:value="formState.materialId" placeholder="选择物料"><Select.Option v-for="m in matStore.materials" :key="m.id" :value="m.id">{{ m.name }}</Select.Option></Select></FormItem>
        <FormItem label="数量" required><InputNumber v-model:value="formState.quantity" :min="0.01" :step="1" style="width:100%" /></FormItem>
        <FormItem v-if="formState.type===1" label="关联采购单"><Input v-model:value="formState.purchaseRequestId" placeholder="请输入采购单ID（可选）" style="width:100%" /></FormItem>
        <FormItem v-if="formState.type===2" label="关联项目"><Input v-model:value="formState.projectId" placeholder="请输入项目ID（可选）" style="width:100%" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>.page-container { background:#fff;padding:24px;border-radius:8px } .page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:20px } .page-header h2 { margin:0;font-size:18px }</style>
