<script setup>
import { Table, Tag } from 'ant-design-vue'
import { usePurchaseOrderList } from '@/composables/usePurchaseOrderList'

const {
  store, auth, pagination, columns,
  showEditAction, showInboundAction, showStatusColumn,
  editVisible, editForm, editError, editSubmitting, openEdit, handleEdit,
  inboundVisible, inboundPO, warehouses, inboundForm, inboundError, inboundSubmitting, openInbound, handleInbound,
  prField, formatDate, formatMoney,
  loadData, handleTableChange,
} = usePurchaseOrderList(null)
</script>

<template>
  <div class="po-list-page">
    <div class="list-header">
      <h2>采购汇总</h2>
    </div>

    <Table
      :columns="columns"
      :data-source="store.items"
      :loading="store.loading"
      row-key="id"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: store.total, showSizeChanger: false }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'code'">{{ prField(record, 'code') }}</template>
        <template v-if="column.key === 'name'">{{ prField(record, 'name') }}</template>
        <template v-if="column.key === 'material'">
          <span>{{ prField(record, 'materialName') || '-' }}</span>
          <span v-if="prField(record, 'materialSpec')" style="color:#9ca3af"> / {{ prField(record, 'materialSpec') }}</span>
        </template>
        <template v-if="column.key === 'spec'">{{ prField(record, 'specification') }}</template>
        <template v-if="column.key === 'qty'">{{ prField(record, 'quantityStr') }}</template>
        <template v-if="column.key === 'supplier'">{{ record.supplierName || '-' }}</template>
        <template v-if="column.key === 'unitPrice'">{{ formatMoney(record.unitPrice) }}</template>
        <template v-if="column.key === 'totalAmount'">{{ formatMoney(record.totalAmount) }}</template>
        <template v-if="column.key === 'purchaseDate'">{{ formatDate(record.purchaseDate) }}</template>
        <template v-if="column.key === 'arrivalDate'">{{ formatDate(record.expectedArrivalDate) }}</template>
        <template v-if="column.key === 'status'">
          <Tag :color="['orange', 'blue', 'green'][record.statusCode]">{{ store.getStatusLabel(record.statusCode) }}</Tag>
        </template>
        <template v-if="column.key === 'action'">
          <span style="color:#9ca3af">-</span>
        </template>
      </template>
    </Table>
  </div>
</template>

<style scoped>
.po-list-page { padding: 24px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.list-header h2 { margin: 0; font-size: 20px; }
</style>
