<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useWarehouseStore } from '@/stores/warehouse'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Tag, Modal, Form, FormItem, Input, Select, Alert, Space, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const store = useWarehouseStore()
const auth = useAuthStore()
const modalVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formState = reactive({ code: '', name: '', location: '' })
const error = ref(null)
const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '编码', dataIndex: 'code' },
  { title: '名称', dataIndex: 'name' },
  { title: '位置', dataIndex: 'location' },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 160 },
]
const rules = { code: [{ required: true, message: '请输入编码' }], name: [{ required: true, message: '请输入名称' }] }

onMounted(() => store.fetchAll())

function openCreate() {
  editingId.value = null; formState.code = ''; formState.name = ''; formState.location = ''; error.value = null; modalVisible.value = true
}
function openEdit(record) { editingId.value = record.id; formState.code = record.code; formState.name = record.name; formState.location = record.location || ''; modalVisible.value = true }
async function handleSubmit() {
  error.value = null; submitting.value = true
  try {
    if (editingId.value) { await store.update(editingId.value, { name: formState.name, location: formState.location }) }
    else { await store.create({ code: formState.code, name: formState.name, location: formState.location }) }
    modalVisible.value = false; store.fetchAll(); message.success('操作成功')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}
function handleDelete(record) {
  Modal.confirm({ title: '确认删除', content: `删除仓库「${record.name}」？`, onOk: async () => { await store.remove(record.id); store.fetchAll() } })
}
</script>

<template>
  <div class="page-container">
    <div class="page-header"><h2>仓库管理</h2>
      <Button v-if="auth.hasPermission('warehouse:create')" type="primary" @click="openCreate"><PlusOutlined />创建仓库</Button>
    </div>
    <Alert v-if="store.error" type="error" :message="store.error" closable style="margin-bottom:16px" />
    <Table :columns="columns" :data-source="store.warehouses" :loading="store.loading" row-key="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'"><Tag :color="record.status==='ACTIVE'?'green':'default'">{{ record.status==='ACTIVE'?'启用':'禁用' }}</Tag></template>
        <template v-if="column.key === 'action'">
          <Space>
            <a v-if="auth.hasPermission('warehouse:update')" @click="openEdit(record)">编辑</a>
            <a v-if="auth.hasPermission('warehouse:delete')" style="color:#ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>
    <Modal v-model:open="modalVisible" :title="editingId?'编辑仓库':'创建仓库'" :confirm-loading="submitting" @ok="handleSubmit">
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
      <Form :model="formState" :rules="rules" layout="vertical">
        <FormItem v-if="!editingId" label="编码" name="code"><Input v-model:value="formState.code" :maxlength="50" /></FormItem>
        <FormItem label="名称" name="name"><Input v-model:value="formState.name" :maxlength="100" /></FormItem>
        <FormItem label="位置"><Input v-model:value="formState.location" :maxlength="200" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>.page-container { background:#fff;padding:24px;border-radius:8px } .page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:20px } .page-header h2 { margin:0;font-size:18px }</style>
