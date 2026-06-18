<script setup>
import { ref, computed, onMounted } from 'vue'
import { Table, Button, Tag, Modal, Alert, Empty } from 'ant-design-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'

const props = defineProps({
  orderId: { type: [Number, String], required: true },
  orderStatus: { type: Number, default: 0 }
})

const emit = defineEmits(['selected'])

const store = usePurchaseOrderStore()
const selecting = ref(false)
const error = ref(null)

const quotations = computed(() => store.quotations || [])
const hasQuotations = computed(() => quotations.value.length > 0)
const hasSelected = computed(() => quotations.value.some(q => q.statusCode === 1))

const columns = [
  { title: '供应商公司', dataIndex: 'supplierCompanyName', key: 'company' },
  { title: '联系人', dataIndex: 'supplierContactPerson', key: 'person' },
  { title: '联系电话', dataIndex: 'supplierContactPhone', key: 'phone' },
  { title: '单价（元）', dataIndex: 'unitPrice', key: 'unitPrice' },
  { title: '总金额（元）', dataIndex: 'totalAmount', key: 'totalAmount' },
  { title: '交货日期', dataIndex: 'deliveryDate', key: 'deliveryDate' },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '报价时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 100 }
]

function formatDate(v) { return v ? new Date(v).toLocaleString('zh-CN') : '-' }
function formatMoney(v) { return v != null ? Number(v).toFixed(2) : '-' }

function handleSelect(record) {
  Modal.confirm({
    title: '确认选用报价',
    content: `确定选用「${record.supplierCompanyName}」的报价吗？选用后采购单将自动流转为「已采购」状态。\n\n单价：${formatMoney(record.unitPrice)} 元\n总金额：${formatMoney(record.totalAmount)} 元`,
    okText: '确认选用',
    cancelText: '取消',
    onOk: async () => {
      selecting.value = true
      error.value = null
      try {
        await store.selectQuotation(props.orderId, record.id)
        emit('selected')
      } catch (e) {
        error.value = e.message || '选用失败'
      } finally {
        selecting.value = false
      }
    }
  })
}
</script>

<template>
  <div class="quotation-list">
    <h3>供应商报价</h3>
    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:12px" @close="error = null" />

    <Table v-if="hasQuotations"
      :columns="columns"
      :data-source="quotations"
      :loading="store.loading"
      :pagination="false"
      row-key="id"
      :scroll="{ x: 1100 }"
      size="middle"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'unitPrice'">{{ formatMoney(record.unitPrice) }}</template>
        <template v-if="column.key === 'totalAmount'">{{ formatMoney(record.totalAmount) }}</template>
        <template v-if="column.key === 'createdAt'">{{ formatDate(record.createdAt) }}</template>
        <template v-if="column.key === 'status'">
          <Tag v-if="record.statusCode === 1" color="green">已选用</Tag>
          <Tag v-else color="blue">待审核</Tag>
        </template>
        <template v-if="column.key === 'action'">
          <Button v-if="record.statusCode !== 1 && !hasSelected" type="link" size="small" @click="handleSelect(record)">选用</Button>
        </template>
      </template>
    </Table>

    <Empty v-else description="暂无供应商报价" />

    <div v-if="hasSelected" style="margin-top:12px">
      <Tag color="green">已有报价被选用，采购单信息已自动更新</Tag>
    </div>
  </div>
</template>

<style scoped>
.quotation-list { margin-top: 32px; }
.quotation-list h3 { margin: 0 0 16px; font-size: 16px; }
</style>
