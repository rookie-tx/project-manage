<script setup>
import { Table, Button, Space, Modal, Form, FormItem, Input, InputNumber, Select, Alert, Tag } from 'ant-design-vue'
import { usePurchaseOrderList } from '@/composables/usePurchaseOrderList'

const {
  store, auth, pagination, columns,
  showEditAction, showInboundAction, showStatusColumn,
  inboundVisible, inboundPO, warehouses, inboundForm, inboundError, inboundSubmitting, openInbound, handleInbound,
  prField, formatDate, formatMoney,
  loadData, handleTableChange,
} = usePurchaseOrderList(1)
</script>

<template>
  <div class="po-list-page">
    <div class="list-header">
      <h2>已采购</h2>
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
        <template v-if="column.key === 'material'">{{ prField(record, 'materialName') || '-' }}</template>
        <template v-if="column.key === 'spec'">{{ prField(record, 'specification') }}</template>
        <template v-if="column.key === 'qty'">{{ prField(record, 'quantityStr') }}</template>
        <template v-if="column.key === 'supplier'">{{ record.supplierName || '-' }}</template>
        <template v-if="column.key === 'unitPrice'">{{ formatMoney(record.unitPrice) }}</template>
        <template v-if="column.key === 'totalAmount'">{{ formatMoney(record.totalAmount) }}</template>
        <template v-if="column.key === 'purchaseDate'">{{ formatDate(record.purchaseDate) }}</template>
        <template v-if="column.key === 'arrivalDate'">{{ formatDate(record.expectedArrivalDate) }}</template>
        <template v-if="column.key === 'purchaser'">{{ record.purchaserName || '-' }}</template>
        <template v-if="column.key === 'inboundOperator'">{{ record.inboundOperatorName || '-' }}</template>
        <template v-if="column.key === 'status'">
          <Tag :color="['orange', 'blue', 'green'][record.statusCode]">{{ store.getStatusLabel(record.statusCode) }}</Tag>
        </template>
        <template v-if="column.key === 'action'">
          <Space>
            <Button v-if="showInboundAction && auth.hasPermission('stock:in')" type="link" size="small" @click="openInbound(record)">入库</Button>
          </Space>
        </template>
      </template>
    </Table>

    <!-- 入库 Modal -->
    <Modal v-model:open="inboundVisible" title="入库操作" :confirm-loading="inboundSubmitting" @ok="handleInbound">
      <Alert v-if="inboundError" type="error" :message="inboundError" closable style="margin-bottom:16px" />
      <Form layout="vertical">
        <FormItem label="仓库" required><Select v-model:value="inboundForm.warehouseId" placeholder="选择仓库" style="width:100%"><Select.Option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</Select.Option></Select></FormItem>
        <FormItem label="物料">
          <Input :value="`${inboundForm.materialLabel}${inboundForm.materialSpec ? ' / ' + inboundForm.materialSpec : ''}`" disabled placeholder="从采购申请自动带入" style="width:100%" />
        </FormItem>
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
