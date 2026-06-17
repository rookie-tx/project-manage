<script setup>
import { Table, Button, Space, Modal, Form, FormItem, Input, InputNumber, DatePicker, Alert, Tag } from 'ant-design-vue'
import { usePurchaseOrderList } from '@/composables/usePurchaseOrderList'

const {
  store, auth, pagination, columns,
  showEditAction, showInboundAction, showStatusColumn,
  editVisible, editForm, editError, editSubmitting, openEdit, handleEdit,
  prField, formatDate, formatMoney,
  loadData, handleTableChange,
} = usePurchaseOrderList(0)
</script>

<template>
  <div class="po-list-page">
    <div class="list-header">
      <h2>待采购</h2>
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
          <Space>
            <Button v-if="showEditAction && auth.hasPermission('purchase:update')" type="link" size="small" @click="openEdit(record)">采购完成</Button>
          </Space>
        </template>
      </template>
    </Table>

    <!-- 采购完成 Modal -->
    <Modal v-model:open="editVisible" title="完善采购信息" :confirm-loading="editSubmitting" okText="确认完成" @ok="handleEdit" width="520px">
      <Alert v-if="editError" type="error" :message="editError" closable style="margin-bottom:16px" />
      <Form layout="vertical">
        <FormItem label="供应商名称" required><Input v-model:value="editForm.supplierName" style="width:100%" /></FormItem>
        <FormItem label="供应商电话"><Input v-model:value="editForm.supplierPhone" style="width:100%" /></FormItem>
        <FormItem label="单价"><InputNumber v-model:value="editForm.unitPrice" :min="0" :precision="2" style="width:100%" /></FormItem>
        <FormItem label="总金额" required><InputNumber v-model:value="editForm.totalAmount" :min="0" :precision="2" style="width:100%" /></FormItem>
        <FormItem label="采购日期"><DatePicker v-model:value="editForm.purchaseDate" style="width:100%" /></FormItem>
        <FormItem label="预计到货日期"><DatePicker v-model:value="editForm.expectedArrivalDate" style="width:100%" /></FormItem>
        <FormItem label="备注"><Input v-model:value="editForm.remark" style="width:100%" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>
.po-list-page { padding: 24px; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.list-header h2 { margin: 0; font-size: 20px; }
</style>
