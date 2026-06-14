<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useSupplierStore } from '@/stores/supplier'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Tag, Modal, Form, FormItem, Input, Select, Alert, Space, message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'

const store = useSupplierStore()
const auth = useAuthStore()
const modalVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const searchKeyword = ref('')
const statusFilter = ref(undefined)
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })

const formState = reactive({
  username: '', password: '',
  companyName: '', licenseNo: '',
  contactPerson: '', contactPhone: '', address: ''
})
const error = ref(null)

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '公司名称', dataIndex: 'companyName' },
  { title: '联系人', dataIndex: 'contactPerson', width: 100 },
  { title: '联系电话', dataIndex: 'contactPhone', width: 130 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 160 },
]

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码', min: 6 }],
  companyName: [{ required: true, message: '请输入公司名称' }],
  contactPerson: [{ required: true, message: '请输入联系人' }],
  contactPhone: [{ required: true, message: '请输入联系电话' }],
}

onMounted(() => loadData())

async function loadData() {
  try {
    await store.fetchList({ page: pagination.current, size: pagination.pageSize, keyword: searchKeyword.value, status: statusFilter.value })
    pagination.total = store.total
  } catch { /* error handled by store */ }
}

function handleSearch() { pagination.current = 1; loadData() }
function handleTableChange(pag) { pagination.current = pag.current; loadData() }

function openCreate() {
  editingId.value = null
  Object.assign(formState, { username: '', password: '', companyName: '', licenseNo: '', contactPerson: '', contactPhone: '', address: '' })
  error.value = null
  modalVisible.value = true
}

async function handleSubmit() {
  error.value = null; submitting.value = true
  try {
    if (editingId.value) {
      await store.update(editingId.value, {
        companyName: formState.companyName, licenseNo: formState.licenseNo,
        contactPerson: formState.contactPerson, contactPhone: formState.contactPhone,
        address: formState.address
      })
    } else {
      await store.create({
        username: formState.username, password: formState.password,
        companyName: formState.companyName, licenseNo: formState.licenseNo,
        contactPerson: formState.contactPerson, contactPhone: formState.contactPhone,
        address: formState.address
      })
    }
    modalVisible.value = false; loadData(); message.success('操作成功')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}

function handleDelete(record) {
  Modal.confirm({ title: '确认删除', content: `删除供应商「${record.companyName}」？`, onOk: async () => { await store.remove(record.id); loadData() } })
}
</script>

<template>
  <div class="page-container">
    <div class="page-header"><h2>供应商管理</h2>
      <Button v-if="auth.hasPermission('supplier:create')" type="primary" @click="openCreate"><PlusOutlined />创建供应商</Button>
    </div>

    <Space style="margin-bottom:16px">
      <Input v-model:value="searchKeyword" placeholder="搜索公司名称" style="width:240px" @pressEnter="handleSearch" />
      <Select v-model:value="statusFilter" placeholder="状态" allowClear style="width:120px" @change="handleSearch">
        <Select.Option :value="1">正常</Select.Option>
        <Select.Option :value="0">禁用</Select.Option>
      </Select>
      <Button @click="handleSearch"><SearchOutlined />搜索</Button>
    </Space>

    <Alert v-if="store.error" type="error" :message="store.error" closable style="margin-bottom:16px" />

    <Table :columns="columns" :data-source="store.records" :loading="store.loading" row-key="id"
      :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <Tag :color="record.status===1?'green':'default'">{{ record.status===1?'正常':'禁用' }}</Tag>
        </template>
        <template v-if="column.key === 'createdAt'">{{ record.createdAt ? record.createdAt.substring(0,10) : '' }}</template>
        <template v-if="column.key === 'action'">
          <Space>
            <a v-if="auth.hasPermission('supplier:update')" @click="openCreate(); editingId=record.id; Object.assign(formState,{companyName:record.companyName,licenseNo:record.licenseNo||'',contactPerson:record.contactPerson,contactPhone:record.contactPhone,address:record.address||''})">编辑</a>
            <a v-if="auth.hasPermission('supplier:delete')" style="color:#ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>

    <Modal v-model:open="modalVisible" :title="editingId?'编辑供应商':'创建供应商'" :confirm-loading="submitting" @ok="handleSubmit" width="560px">
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
      <Form :model="formState" :rules="rules" layout="vertical">
        <FormItem v-if="!editingId" label="用户名" name="username"><Input v-model:value="formState.username" :maxlength="50" /></FormItem>
        <FormItem v-if="!editingId" label="密码" name="password"><Input v-model:value="formState.password" type="password" :maxlength="50" /></FormItem>
        <FormItem label="公司名称" name="companyName"><Input v-model:value="formState.companyName" :maxlength="200" /></FormItem>
        <FormItem label="统一社会信用代码"><Input v-model:value="formState.licenseNo" :maxlength="100" /></FormItem>
        <FormItem label="联系人" name="contactPerson"><Input v-model:value="formState.contactPerson" :maxlength="50" /></FormItem>
        <FormItem label="联系电话" name="contactPhone"><Input v-model:value="formState.contactPhone" :maxlength="30" /></FormItem>
        <FormItem label="地址"><Input v-model:value="formState.address" :maxlength="300" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>.page-container{background:#fff;padding:24px;border-radius:8px}.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}.page-header h2{margin:0;font-size:18px}</style>
