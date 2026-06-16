<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMaterialStore, useWarehouseStore } from '@/stores/warehouse'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Tag, Modal, Form, FormItem, Input, Select, Alert, Space, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const store = useMaterialStore()
const whStore = useWarehouseStore()
const auth = useAuthStore()
const modalVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formState = reactive({ code: '', name: '', spec: '', unit: null })
const error = ref(null)
const stockModal = ref(false)
const stockData = ref([])
const stockWarehouse = ref('')
const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '编码', dataIndex: 'code' },
  { title: '名称', dataIndex: 'name' },
  { title: '规格', dataIndex: 'spec' },
  { title: '单位', dataIndex: 'unit', width: 80 },
  { title: '操作', key: 'action', width: 200 },
]
const unitOptions = [
  { value: 0, label: '个' }, { value: 1, label: '台' }, { value: 2, label: '套' },
  { value: 3, label: '米' }, { value: 4, label: '吨' }, { value: 5, label: '立方米' },
  { value: 6, label: '卷' }, { value: 7, label: '桶' }, { value: 8, label: '箱' }, { value: 9, label: '其他' },
]
const rules = {
  code: [{ required: true, message: '请输入编码' }],
  name: [{ required: true, message: '请输入名称' }],
  spec: [{ required: true, message: '请输入规格' }],
  unit: [{ required: true, message: '请选择单位' }],
}

onMounted(() => { store.fetchAll(); whStore.fetchAll() })

function openCreate() { editingId.value = null; formState.code = ''; formState.name = ''; formState.spec = ''; formState.unit = null; error.value = null; modalVisible.value = true }
function openEdit(record) { editingId.value = record.id; formState.code = record.code; formState.name = record.name; formState.spec = record.spec || ''; formState.unit = record.unit != null ? record.unit : null; modalVisible.value = true }
function getUnitLabel(code) { const opt = unitOptions.find(o => o.value === code); return opt ? opt.label : code }
async function handleSubmit() {
  error.value = null; submitting.value = true
  try {
    if (editingId.value) { await store.update(editingId.value, { name: formState.name, spec: formState.spec, unit: formState.unit }) }
    else { await store.create({ code: formState.code, name: formState.name, spec: formState.spec, unit: formState.unit }) }
    modalVisible.value = false; store.fetchAll(); message.success('操作成功')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}
async function showStock(record) {
  const data = await store.fetchStock(record.id)
  stockData.value = data.map(d => ({ warehouse: whStore.warehouses.find(w => w.id === d.warehouseId)?.name || '未知', quantity: d.quantity }))
  stockWarehouse.value = record.name; stockModal.value = true
}
function handleDelete(record) {
  Modal.confirm({ title: '确认删除', content: `删除物料「${record.name}」？`, onOk: async () => { await store.remove(record.id); store.fetchAll() } })
}
</script>

<template>
  <div class="page-container">
    <div class="page-header"><h2>物料管理</h2>
      <Button v-if="auth.hasPermission('material:create')" type="primary" @click="openCreate"><PlusOutlined />创建物料</Button>
    </div>
    <Table :columns="columns" :data-source="store.materials" :loading="store.loading" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'unit'">
          {{ getUnitLabel(record.unit) }}
        </template>
        <template v-if="column.key === 'action'">
          <Space>
            <a @click="showStock(record)">库存</a>
            <a v-if="auth.hasPermission('material:update')" @click="openEdit(record)">编辑</a>
            <a v-if="auth.hasPermission('material:delete')" style="color:#ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>

    <Modal v-model:open="modalVisible" :title="editingId?'编辑物料':'创建物料'" :confirm-loading="submitting" @ok="handleSubmit">
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
      <Form :model="formState" :rules="rules" layout="vertical">
        <FormItem v-if="!editingId" label="编码" name="code"><Input v-model:value="formState.code" :maxlength="50" /></FormItem>
        <FormItem label="名称" name="name"><Input v-model:value="formState.name" :maxlength="100" /></FormItem>
        <FormItem label="规格" name="spec"><Input v-model:value="formState.spec" :maxlength="100" /></FormItem>
        <FormItem label="单位" name="unit">
          <Select v-model:value="formState.unit" placeholder="请选择单位" :options="unitOptions" />
        </FormItem>
      </Form>
    </Modal>

    <Modal v-model:open="stockModal" :title="`${stockWarehouse} - 各仓库库存`" :footer="null">
      <Table :columns="[{title:'仓库',dataIndex:'warehouse'},{title:'库存量',dataIndex:'quantity'}]" :data-source="stockData" row-key="warehouse" :pagination="false" size="small" />
    </Modal>
  </div>
</template>

<style scoped>.page-container { background:#fff;padding:24px;border-radius:8px } .page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:20px } .page-header h2 { margin:0;font-size:18px }</style>
