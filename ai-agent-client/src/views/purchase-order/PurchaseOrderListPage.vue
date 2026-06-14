<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Space, Modal, Form, FormItem, InputNumber, DatePicker, Tabs, message } from 'ant-design-vue'
import client from '@/api/client'
import dayjs from 'dayjs'

const store = usePurchaseOrderStore()
const auth = useAuthStore()

const activeTab = ref('pending')
const pagination = reactive({ current: 1, pageSize: 20 })
const tabItems = [
  { key: 'pending', label: '待采购' },
  { key: 'purchased', label: '已采购' },
  { key: 'arrived', label: '已到货' },
]

// Edit modal
const editVisible = ref(false)
const editingPO = ref(null)
const editForm = reactive({ supplierName: '', supplierPhone: '', unitPrice: null, totalAmount: null, purchaseDate: null, expectedArrivalDate: null, remark: '' })
const editError = ref(null)
const editSubmitting = ref(false)

// Inbound modal
const inboundVisible = ref(false)
const inboundPO = ref(null)
const warehouses = ref([])
const materials = ref([])
const inboundForm = reactive({ warehouseId: null, materialId: null, quantity: 1 })
const inboundError = ref(null)
const inboundSubmitting = ref(false)

const columns = [
  { title: '编号', key: 'code', width: 120 },
  { title: '名称', key: 'name', width: 160 },
  { title: '规格', key: 'spec', width: 120 },
  { title: '数量', key: 'qty', width: 80 },
  { title: '供应商', key: 'supplier', width: 80 },
  { title: '单价', key: 'unitPrice', width: 100 },
  { title: '总金额', key: 'totalAmount', width: 100 },
  { title: '采购日期', key: 'purchaseDate', width: 110 },
  { title: '预计到货', key: 'arrivalDate', width: 110 },
  { title: '状态', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120 },
]

function prField(po, field) {
  const p = po?.purchaseRequests?.[0]
  if (!p) return '-'
  return p[field] || '-'
}

function formatDate(d) { return d ? String(d).substring(0, 10) : '-' }
function formatMoney(v) { return v != null ? '¥' + Number(v).toLocaleString() : '-' }

const tabParam = computed(() => {
  if (activeTab.value === 'pending') return 'pending'
  return undefined
})
const statusParam = computed(() => {
  if (activeTab.value === 'purchased') return 1
  if (activeTab.value === 'arrived') return 2
  return undefined
})

onMounted(() => loadData())

async function loadData() {
  await store.fetchList({ tab: tabParam.value, status: statusParam.value, page: pagination.current, size: pagination.pageSize })
}

async function handleTabChange(key) { activeTab.value = key; pagination.current = 1; await loadData() }
async function handleTableChange(pag) { pagination.current = pag.current; await loadData() }

// --- Edit ---
function openEdit(record) {
  editingPO.value = record
  Object.assign(editForm, {
    supplierName: record.supplierName || '', supplierPhone: record.supplierPhone || '',
    unitPrice: record.unitPrice, totalAmount: record.totalAmount,
    purchaseDate: record.purchaseDate ? dayjs(record.purchaseDate) : null,
    expectedArrivalDate: record.expectedArrivalDate ? dayjs(record.expectedArrivalDate) : null,
    remark: record.remark || ''
  })
  editError.value = null; editVisible.value = true
}
async function handleEdit() {
  editError.value = null; editSubmitting.value = true
  try {
    const payload = {
      supplierName: editForm.supplierName, supplierPhone: editForm.supplierPhone,
      unitPrice: editForm.unitPrice, totalAmount: editForm.totalAmount,
      purchaseDate: editForm.purchaseDate ? dayjs(editForm.purchaseDate).format('YYYY-MM-DD') : null,
      expectedArrivalDate: editForm.expectedArrivalDate ? dayjs(editForm.expectedArrivalDate).format('YYYY-MM-DD') : null,
      remark: editForm.remark
    }
    await store.update(editingPO.value.id, payload)
    await store.changeStatus(editingPO.value.id, 1)  // 直接流转到已采购
    editVisible.value = false; await loadData(); message.success('采购完成')
  } catch (e) { editError.value = e.message }
  finally { editSubmitting.value = false }
}

// --- Inbound ---
async function openInbound(record) {
  inboundPO.value = record
  inboundForm.warehouseId = null; inboundForm.materialId = null; inboundForm.quantity = 1
  inboundError.value = null
  try {
    const [wRes, mRes] = await Promise.all([client.get('/warehouses'), client.get('/materials')])
    warehouses.value = wRes.data || []; materials.value = mRes.data || []
  } catch { warehouses.value = []; materials.value = [] }
  inboundVisible.value = true
}
async function handleInbound() {
  const f = inboundForm
  if (!f.warehouseId) { inboundError.value = '请选择仓库'; return }
  if (!f.materialId) { inboundError.value = '请选择物料'; return }
  if (!f.quantity || f.quantity <= 0) { inboundError.value = '请输入有效数量'; return }
  inboundSubmitting.value = true
  try {
    await client.post('/stock-records', { type: 1, warehouseId: f.warehouseId, materialId: f.materialId, quantity: f.quantity, purchaseRequestId: inboundPO.value.purchaseRequestId })
    inboundVisible.value = false; await loadData(); message.success('入库完成')
  } catch (e) { inboundError.value = e.message }
  finally { inboundSubmitting.value = false }
}

</script>

<template>
  <div class="po-list-page">
    <div class="list-header">
      <h2>采购管理</h2>
    </div>

    <Tabs v-model:activeKey="activeTab" :items="tabItems" @change="handleTabChange" style="margin-bottom:16px" />

    <Table :columns="columns" :data-source="store.items" :loading="store.loading" row-key="id"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: store.total, showSizeChanger: false }"
      @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'code'">{{ prField(record, 'code') }}</template>
        <template v-if="column.key === 'name'">{{ prField(record, 'name') }}</template>
        <template v-if="column.key === 'spec'">{{ prField(record, 'specification') }}</template>
        <template v-if="column.key === 'qty'">{{ prField(record, 'quantityStr') }}</template>
        <template v-if="column.key === 'supplier'">{{ record.supplierName || '-' }}</template>
        <template v-if="column.key === 'unitPrice'">{{ formatMoney(record.unitPrice) }}</template>
        <template v-if="column.key === 'totalAmount'">{{ formatMoney(record.totalAmount) }}</template>
        <template v-if="column.key === 'purchaseDate'">{{ formatDate(record.purchaseDate) }}</template>
        <template v-if="column.key === 'arrivalDate'">{{ formatDate(record.expectedArrivalDate) }}</template>
        <template v-if="column.key === 'status'">{{ store.getStatusLabel(record.statusCode) }}</template>
        <template v-if="column.key === 'action'">
          <Space>
            <Button v-if="activeTab === 'pending' && auth.hasPermission('purchase:update')" type="link" size="small" @click="openEdit(record)">采购完成</Button>
            <Button v-if="activeTab === 'purchased' && auth.hasPermission('stock:in')" type="link" size="small" @click="openInbound(record)">入库</Button>
          </Space>
        </template>
      </template>
    </Table>

    <!-- Edit Modal -->
    <Modal v-model:open="editVisible" title="完善采购信息" :confirm-loading="editSubmitting" okText="确认完成" @ok="handleEdit" width="520px">
      <Space v-if="editError" style="color:#ff4d4f;margin-bottom:12px">{{ editError }}</Space>
      <Form layout="vertical">
        <FormItem label="供应商名称" required><Input v-model:value="editForm.supplierName" /></FormItem>
        <FormItem label="供应商电话"><Input v-model:value="editForm.supplierPhone" /></FormItem>
        <FormItem label="单价"><InputNumber v-model:value="editForm.unitPrice" :min="0" :precision="2" style="width:100%" /></FormItem>
        <FormItem label="总金额" required><InputNumber v-model:value="editForm.totalAmount" :min="0" :precision="2" style="width:100%" /></FormItem>
        <FormItem label="采购日期"><DatePicker v-model:value="editForm.purchaseDate" style="width:100%" /></FormItem>
        <FormItem label="预计到货日期"><DatePicker v-model:value="editForm.expectedArrivalDate" style="width:100%" /></FormItem>
        <FormItem label="备注"><Input v-model:value="editForm.remark" /></FormItem>
      </Form>
    </Modal>

    <!-- Inbound Modal -->
    <Modal v-model:open="inboundVisible" title="入库操作" :confirm-loading="inboundSubmitting" @ok="handleInbound">
      <Space v-if="inboundError" style="color:#ff4d4f;margin-bottom:12px">{{ inboundError }}</Space>
      <Form layout="vertical">
        <FormItem label="仓库" required><Select v-model:value="inboundForm.warehouseId" placeholder="选择仓库"><Select.Option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</Select.Option></Select></FormItem>
        <FormItem label="物料" required><Select v-model:value="inboundForm.materialId" placeholder="选择物料"><Select.Option v-for="m in materials" :key="m.id" :value="m.id">{{ m.name }}</Select.Option></Select></FormItem>
        <FormItem label="数量" required><InputNumber v-model:value="inboundForm.quantity" :min="0.01" style="width:100%" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>
.po-list-page { padding: 24px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.list-header h2 { margin: 0; font-size: 20px; }
</style>
