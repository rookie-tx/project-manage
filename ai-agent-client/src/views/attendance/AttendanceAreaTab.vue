<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Table, Button, Modal, Form, FormItem, Input, InputNumber, Space, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import client from '@/api/client'
import AmapPicker from '@/components/common/AmapPicker.vue'

const props = defineProps({ projectId: { type: Number, required: true } })

const areas = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formState = reactive({ name: '', centerLat: null, centerLng: null, radius: 500 })

// 地图初始中心（默认北京天安门附近）
const defaultLat = 39.909187
const defaultLng = 116.397428

const columns = [
  { title: '区域名称', dataIndex: 'name', width: 150 },
  { title: '中心纬度', dataIndex: 'centerLat', width: 130 },
  { title: '中心经度', dataIndex: 'centerLng', width: 130 },
  { title: '半径（米）', dataIndex: 'radius', width: 100 },
  { title: '操作', key: 'action', width: 120 },
]

onMounted(() => { fetchAreas() })

async function fetchAreas() {
  loading.value = true
  try {
    const { data } = await client.get(`/projects/${props.projectId}/attendance-areas`)
    areas.value = data || []
  } catch (e) { areas.value = [] }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  formState.name = ''
  formState.centerLat = null
  formState.centerLng = null
  formState.radius = 500
  modalVisible.value = true
}

function openEdit(record) {
  isEdit.value = true
  editingId.value = record.id
  formState.name = record.name
  formState.centerLat = record.centerLat
  formState.centerLng = record.centerLng
  formState.radius = record.radius
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formState.name || formState.centerLat == null || formState.centerLng == null) {
    message.warning('请在地图上点击选择打卡中心点')
    return
  }
  if (formState.radius < 50 || formState.radius > 5000) {
    message.warning('半径需在 50-5000 米之间')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await client.put(`/attendance-areas/${editingId.value}`, {
        name: formState.name,
        centerLat: formState.centerLat,
        centerLng: formState.centerLng,
        radius: formState.radius,
      })
      message.success('更新成功')
    } else {
      await client.post(`/projects/${props.projectId}/attendance-areas`, {
        name: formState.name,
        centerLat: formState.centerLat,
        centerLng: formState.centerLng,
        radius: formState.radius,
      })
      message.success('创建成功')
    }
    modalVisible.value = false
    fetchAreas()
  } catch (e) { message.error(e.message) }
  finally { submitting.value = false }
}

function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除打卡区域「${record.name}」？`,
    async onOk() {
      try {
        await client.delete(`/attendance-areas/${record.id}`)
        message.success('删除成功')
        fetchAreas()
      } catch (e) { message.error(e.message) }
    },
  })
}

/** 地图选点后同步坐标 */
function onMapUpdateLat(val) { formState.centerLat = val }
function onMapUpdateLng(val) { formState.centerLng = val }

/** 获取地图显示的坐标（未选点时用默认） */
function getMapLat() { return formState.centerLat != null ? formState.centerLat : defaultLat }
function getMapLng() { return formState.centerLng != null ? formState.centerLng : defaultLng }
</script>

<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span style="color:#666;font-size:13px">配置后，项目下的临时工只有在打卡区域内才能完成打卡</span>
      <Button type="primary" @click="openCreate"><PlusOutlined /> 新增打卡区域</Button>
    </div>

    <Table :columns="columns" :data-source="areas" :loading="loading" row-key="id"
           :pagination="false" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Space>
            <a @click="openEdit(record)">编辑</a>
            <a style="color:#ff4d4f" @click="handleDelete(record)">删除</a>
          </Space>
        </template>
      </template>
    </Table>

    <div v-if="!loading && areas.length === 0" style="text-align:center;padding:48px;color:#999">
      暂无打卡区域配置
    </div>

    <Modal v-model:open="modalVisible" :title="isEdit ? '编辑打卡区域' : '新增打卡区域'"
           :confirm-loading="submitting" @ok="handleSubmit" width="640px" :destroy-on-close="true">
      <Form layout="vertical">
        <FormItem label="区域名称" required>
          <Input v-model:value="formState.name" maxlength="100" placeholder="如：工地正门" />
        </FormItem>

        <FormItem label="打卡半径（米）" required>
          <InputNumber v-model:value="formState.radius" :min="50" :max="5000" :step="50"
                       style="width:100%" placeholder="50-5000 米" />
        </FormItem>

        <FormItem label="打卡中心点（在地图上点击选择）" required>
          <AmapPicker
            :lat="getMapLat()"
            :lng="getMapLng()"
            :radius="formState.radius"
            @update:lat="onMapUpdateLat"
            @update:lng="onMapUpdateLng"
          />
        </FormItem>

        <!-- 手动微调坐标 -->
        <div style="display:flex;gap:12px">
          <FormItem label="当前纬度" style="flex:1">
            <InputNumber v-model:value="formState.centerLat" :min="-90" :max="90" :step="0.000001"
                         style="width:100%" placeholder="点击地图自动填入" :precision="6" />
          </FormItem>
          <FormItem label="当前经度" style="flex:1">
            <InputNumber v-model:value="formState.centerLng" :min="-180" :max="180" :step="0.000001"
                         style="width:100%" placeholder="点击地图自动填入" :precision="6" />
          </FormItem>
        </div>
      </Form>
    </Modal>
  </div>
</template>
