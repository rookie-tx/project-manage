<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Table, Button, Tag, Modal, Form, FormItem, Input, DatePicker, Select, Alert, Space, Divider, message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined } from '@ant-design/icons-vue'
import client from '@/api/client'

const props = defineProps({ projectId: { type: Number, required: true } })

const workers = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const error = ref(null)
const formState = reactive({ name: '', phone: '', workerType: '', entryDate: null, exitDate: null })

// Attendance
const attendanceRecords = ref([])
const attLoading = ref(false)
const attMonth = ref(dayjs())

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '手机号', dataIndex: 'phone', width: 130 },
  { title: '工种', dataIndex: 'workerType' },
  { title: '入职日期', dataIndex: 'entryDate', width: 120 },
  { title: '离职日期', dataIndex: 'exitDate', width: 120 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 160 },
]

const attColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '日期', key: 'date', width: 110 },
  { title: '上班', key: 'in', width: 80 },
  { title: '下班', key: 'out', width: 80 },
  { title: '状态', key: 'status', width: 80 },
]

onMounted(() => { fetchWorkers(); fetchAttendance() })

async function fetchWorkers() {
  loading.value = true
  try { const { data } = await client.get(`/projects/${props.projectId}/temp-workers`); workers.value = data }
  catch (e) { error.value = e.message }
  finally { loading.value = false }
}

async function fetchAttendance() {
  attLoading.value = true
  try {
    const ym = attMonth.value.format('YYYY-MM')
    const { data } = await client.get(`/attendance/projects/${props.projectId}?yearMonth=${ym}`)
    attendanceRecords.value = data || []
  } catch { attendanceRecords.value = [] }
  finally { attLoading.value = false }
}

function workerName(workerId) { const w = workers.value.find(w => w.id === workerId); return w?.name || w?.username || '未知' }
function formatTime(d) { if (!d) return '--:--'; return d.substring(11, 16) }
function formatDate(d) { if (!d) return ''; return d.substring(0, 10) }

function openCreate() {
  isEdit.value = false; editingId.value = null
  formState.name = ''; formState.phone = ''; formState.workerType = ''; formState.entryDate = null; formState.exitDate = null
  error.value = null; modalVisible.value = true
}

function openEdit(record) {
  isEdit.value = true; editingId.value = record.id; error.value = null
  formState.name = ''; formState.phone = ''; formState.workerType = record.workerType || ''
  formState.entryDate = record.entryDate ? dayjs(record.entryDate) : null
  formState.exitDate = record.exitDate ? dayjs(record.exitDate) : null
  modalVisible.value = true
}

function toDateStr(v) {
  if (!v) return undefined
  if (typeof v === 'string') return v.substring(0, 10)
  return dayjs(v).format('YYYY-MM-DD')
}

async function handleSubmit() {
  error.value = null; submitting.value = true
  try {
    if (isEdit.value) {
      await client.put(`/temp-workers/${editingId.value}`, { workerType: formState.workerType, entryDate: toDateStr(formState.entryDate), exitDate: toDateStr(formState.exitDate) })
    } else {
      await client.post('/temp-workers', { projectId: props.projectId, name: formState.name, phone: formState.phone, workerType: formState.workerType, entryDate: toDateStr(formState.entryDate) })
    }
    modalVisible.value = false; fetchWorkers(); message.success('操作成功')
  } catch (e) { error.value = e.message } finally { submitting.value = false }
}

function handleDelete(record) {
  Modal.confirm({ title: '确认删除', content: `确认删除该临时工？`, onOk: async () => { await client.delete(`/temp-workers/${record.id}`); fetchWorkers() } })
}

function getStatusTag(status) {
  if (status === 'ACTIVE') return { color: 'green', text: '在职' }
  return { color: 'default', text: '离职' }
}
</script>

<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span></span>
      <Button type="primary" @click="openCreate"><PlusOutlined />新增临时工</Button>
    </div>
    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />

    <Table :columns="columns" :data-source="workers" :loading="loading" row-key="id" :pagination="false" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">{{ record.name || record.username }}</template>
        <template v-if="column.key === 'status'"><Tag :color="getStatusTag(record.status).color">{{ getStatusTag(record.status).text }}</Tag></template>
        <template v-if="column.key === 'action'">
          <Space>
            <a @click="openEdit(record)">编辑</a>
            <a style="color:#ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>

    <Divider />
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
      <h4 style="margin:0">打卡记录</h4>
      <DatePicker v-model:value="attMonth" picker="month" @change="fetchAttendance" size="small" style="width:120px" />
    </div>
    <Table :columns="attColumns" :data-source="attendanceRecords" :loading="attLoading" row-key="id" size="small" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">{{ workerName(record.workerId) }}</template>
        <template v-if="column.key === 'date'">{{ formatDate(record.clockIn) }}</template>
        <template v-if="column.key === 'in'">{{ formatTime(record.clockIn) }}</template>
        <template v-if="column.key === 'out'">{{ formatTime(record.clockOut) }}</template>
        <template v-if="column.key === 'status'"><Tag :color="record.status==='CLOCKED_OUT'?'green':'orange'">{{ record.status==='CLOCKED_OUT'?'已下班':'已上班' }}</Tag></template>
      </template>
    </Table>

    <Modal v-model:open="modalVisible" :title="isEdit?'编辑临时工':'新增临时工'" :confirm-loading="submitting" @ok="handleSubmit">
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
      <Form layout="vertical">
        <FormItem v-if="!isEdit" label="姓名" required><Input v-model:value="formState.name" maxlength="50" placeholder="真实姓名" /></FormItem>
        <FormItem v-if="!isEdit" label="手机号" required><Input v-model:value="formState.phone" maxlength="11" placeholder="将作为登录账号" /></FormItem>
        <FormItem label="工种"><Input v-model:value="formState.workerType" maxlength="50" placeholder="如：电工、焊工" /></FormItem>
        <FormItem label="入职日期" required><DatePicker v-model:value="formState.entryDate" style="width:100%" /></FormItem>
        <FormItem v-if="isEdit" label="离职日期"><DatePicker v-model:value="formState.exitDate" style="width:100%" placeholder="设置后自动离职" /></FormItem>
      </Form>
    </Modal>
  </div>
</template>
