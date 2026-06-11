<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useConstructionWasteStore } from '@/stores/constructionWaste'
import client from '@/api/client'
import { uploadFiles, getFileUrl } from '@/api/fileUpload'
import { Form, FormItem, Input, InputNumber, Select, DatePicker, Button, Upload, Alert, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const store = useConstructionWasteStore()
const router = useRouter()

const projects = ref([])
const submitting = ref(false)
const error = ref(null)
const fileList = ref([])
const uploading = ref(false)

const formState = reactive({
  projectId: null,
  wasteType: '',
  quantity: null,
  unit: '',
  vehiclePlate: '',
  driverName: '',
  destination: '',
  removalDate: null,
  remark: '',
})

onMounted(async () => {
  try {
    const res = await client.get('/projects/select')
    projects.value = res.data
  } catch { /* ignore */ }
})

async function handleUpload(info) {
  if (info.file.status === 'done') {
    fileList.value.push(info.file)
  }
}

function handleRemove(file) {
  const idx = fileList.value.indexOf(file)
  if (idx >= 0) fileList.value.splice(idx, 1)
}

async function handleSubmit() {
  error.value = null

  if (!formState.projectId) { error.value = '请选择项目'; return }
  if (!formState.wasteType.trim()) { error.value = '请输入废渣种类'; return }
  if (!formState.quantity || formState.quantity <= 0) { error.value = '请输入有效数量'; return }
  if (!formState.unit.trim()) { error.value = '请输入单位'; return }
  if (!formState.removalDate) { error.value = '请选择运出日期'; return }
  if (fileList.value.length === 0) { error.value = '至少需要上传一张现场照片'; return }

  submitting.value = true

  try {
    // 先上传照片获取 fileIds
    const originFiles = fileList.value.map(f => f.originFileObj || f)
    const fileIds = await uploadFiles(originFiles, 'construction_waste')

    // 提交表单
    const payload = {
      projectId: formState.projectId,
      wasteType: formState.wasteType.trim(),
      quantity: formState.quantity,
      unit: formState.unit.trim(),
      vehiclePlate: formState.vehiclePlate.trim() || null,
      driverName: formState.driverName.trim() || null,
      destination: formState.destination.trim() || null,
      removalDate: formState.removalDate.format ? formState.removalDate.format('YYYY-MM-DD') : formState.removalDate,
      remark: formState.remark.trim() || null,
      fileIds,
    }

    await store.create(payload)
    message.success('运出记录创建成功')
    router.push('/construction-wastes')
  } catch (e) {
    error.value = e.message || '创建失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>新建建筑废渣运出记录</h2>
      <Button @click="router.push('/construction-wastes')">返回列表</Button>
    </div>

    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />

    <Form layout="vertical" style="max-width:640px">
      <FormItem label="所属项目" required>
        <Select v-model:value="formState.projectId" placeholder="选择项目" show-search
          :filter-option="(input, option) => option.label.toLowerCase().includes(input.toLowerCase())"
          :options="projects.map(p => ({ value: p.id, label: `${p.code} ${p.name}` }))" />
      </FormItem>

      <FormItem label="废渣种类" required>
        <Input v-model:value="formState.wasteType" placeholder="如：混凝土块、砖块、土方" />
      </FormItem>

      <div style="display:flex;gap:16px">
        <FormItem label="数量" required style="flex:1">
          <InputNumber v-model:value="formState.quantity" :min="0.01" :step="1" placeholder="数量" style="width:100%" />
        </FormItem>
        <FormItem label="单位" required style="flex:1">
          <Input v-model:value="formState.unit" placeholder="如：吨、方、车" />
        </FormItem>
      </div>

      <div style="display:flex;gap:16px">
        <FormItem label="车牌号" style="flex:1">
          <Input v-model:value="formState.vehiclePlate" placeholder="运输车辆车牌号" />
        </FormItem>
        <FormItem label="司机姓名" style="flex:1">
          <Input v-model:value="formState.driverName" placeholder="运输司机姓名" />
        </FormItem>
      </div>

      <FormItem label="处置去向">
        <Input v-model:value="formState.destination" placeholder="如：城南建筑垃圾处理厂" />
      </FormItem>

      <FormItem label="运出日期" required>
        <DatePicker v-model:value="formState.removalDate" placeholder="选择运出日期" style="width:100%" />
      </FormItem>

      <FormItem label="备注">
        <Input.TextArea v-model:value="formState.remark" placeholder="备注信息（可选）" :rows="3" />
      </FormItem>

      <FormItem label="现场照片" required>
        <Upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :multiple="true"
          :before-upload="() => false"
          @change="handleUpload"
          @remove="handleRemove"
        >
          <div>
            <PlusOutlined />
            <div style="margin-top:8px">选择图片</div>
          </div>
        </Upload>
        <div style="color:#999;font-size:12px">支持 JPG、PNG 格式，至少上传一张</div>
      </FormItem>

      <FormItem>
        <Button type="primary" :loading="submitting" @click="handleSubmit" style="margin-right:12px">提交记录</Button>
        <Button @click="router.push('/construction-wastes')">取消</Button>
      </FormItem>
    </Form>
  </div>
</template>

<style scoped>
.page-container { background:#fff;padding:24px;border-radius:8px }
.page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:24px }
.page-header h2 { margin:0;font-size:18px }
</style>
