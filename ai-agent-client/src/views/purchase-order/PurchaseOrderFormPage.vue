<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Form, FormItem, Input, InputNumber, DatePicker, Select, Button, Upload, Alert, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'
import { uploadFiles } from '@/api/fileUpload'
import client from '@/api/client'
import dayjs from 'dayjs'

const router = useRouter(); const route = useRoute()
const store = usePurchaseOrderStore()
const isEdit = !!route.params.id

const approvedPRs = ref([]); const submitting = ref(false); const error = ref(null)
const fileList = ref([])

const selectedRequestIds = ref([])
const formState = reactive({
  supplierName: '', supplierPhone: '',
  unitPrice: null, totalAmount: null, purchaseDate: null,
  expectedArrivalDate: null, remark: '',
})

onMounted(async () => {
  try { const { data } = await client.get('/purchase-requests/available'); approvedPRs.value = Array.isArray(data) ? data : [] }
  catch { /* ignore */ }
  if (isEdit) {
    try { await store.fetchDetail(route.params.id)
      const c = store.current
      if (c) {
        selectedRequestIds.value = (c.purchaseRequests || []).map(pr => pr.id)
        // 编辑时把已关联的PR补充到选项中（available API 不会返回）
        const linkedPRs = (c.purchaseRequests || []).filter(pr => !approvedPRs.value.some(a => a.id === pr.id))
        approvedPRs.value = [...approvedPRs.value, ...linkedPRs.map(pr => ({ id: pr.id, code: pr.code, name: pr.name, projectName: pr.projectName||'', specification: pr.specification||'', quantity: pr.quantityStr||'', unitLabel: '', arrivalDate: pr.arrivalDate||'' }))]
        formState.supplierName = c.supplierName; formState.supplierPhone = c.supplierPhone || ''
        formState.unitPrice = c.unitPrice; formState.totalAmount = c.totalAmount
        formState.purchaseDate = c.purchaseDate ? dayjs(c.purchaseDate) : null
        formState.expectedArrivalDate = c.expectedArrivalDate ? dayjs(c.expectedArrivalDate) : null
        formState.remark = c.remark || ''
        // 回显已有附件
        if (c.files && c.files.length > 0) {
          const API_BASE = client.defaults.baseURL?.replace(/\/api$/, '') || ''
          fileList.value = c.files.map(f => ({
            uid: String(f.id), name: f.originalName, status: 'done', url: API_BASE + f.url
          }))
        }
      }
    } catch { /* ignore */ }
  }
})

function handleCancel() { router.push('/purchase-orders') }

async function handleSubmit() {
  error.value = null
  if (!isEdit && selectedRequestIds.value.length === 0) { error.value = '请至少选择一个采购申请'; return }
  if (!formState.supplierName.trim()) { error.value = '请输入供应商名称'; return }
  if (!formState.totalAmount || formState.totalAmount <= 0) { error.value = '请输入有效总金额'; return }
  if (!formState.purchaseDate) { error.value = '请选择采购日期'; return }
  if (!isEdit && fileList.value.length === 0) { error.value = '至少需要上传一个附件'; return }

  submitting.value = true
  try {
    let fileIds = []
    if (!isEdit && fileList.value.length > 0) {
      const originFiles = fileList.value.map(f => f.originFileObj || f)
      fileIds = await uploadFiles(originFiles, 'purchase_order')
    } else if (isEdit && fileList.value.length > 0) {
      const newFiles = fileList.value.filter(f => !f.url).map(f => f.originFileObj || f)
      if (newFiles.length > 0) fileIds = await uploadFiles(newFiles, 'purchase_order')
    }
    const payload = {
      supplierName: formState.supplierName.trim(),
      supplierPhone: formState.supplierPhone.trim() || null,
      unitPrice: formState.unitPrice,
      totalAmount: formState.totalAmount,
      purchaseDate: formState.purchaseDate.format ? formState.purchaseDate.format('YYYY-MM-DD') : formState.purchaseDate,
      expectedArrivalDate: formState.expectedArrivalDate ? (formState.expectedArrivalDate.format ? formState.expectedArrivalDate.format('YYYY-MM-DD') : formState.expectedArrivalDate) : null,
      remark: formState.remark.trim() || null,
    }
    payload.purchaseRequestIds = selectedRequestIds.value
    if (fileIds.length > 0) {
      payload.fileIds = fileIds
    }

    if (isEdit) {
      await store.update(route.params.id, payload)
    } else {
      await store.create(payload)
    }
    message.success(isEdit ? '修改成功' : '创建成功')
    router.push('/purchase-orders')
  } catch (e) { error.value = e.message || '提交失败' }
  finally { submitting.value = false }
}

function handleUpload(info) { if (info.file.status === 'done') fileList.value.push(info.file) }
function handleRemove(file) { fileList.value = fileList.value.filter(f => f !== file) }
</script>

<template>
  <div class="po-form-page">
    <h2>{{ isEdit ? '编辑采购记录' : '新建采购记录' }}</h2>
    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />
    <Form layout="vertical" style="max-width:640px">
      <FormItem label="采购申请" required>
        <Select v-model:value="selectedRequestIds" mode="multiple" placeholder="选择已通过的采购申请" show-search option-filter-prop="label" style="width:100%"
          :options="approvedPRs.map(p => ({ value: p.id, label: p.code + ' ' + p.name + ' | ' + (p.projectName||'') + ' | ' + (p.specification||'-') + ' | ' + p.quantity + (p.unitLabel||'') + ' | ' + (p.arrivalDate||'') }))" />
      </FormItem>
      <FormItem label="供应商名称" required><Input v-model:value="formState.supplierName" placeholder="供应商公司名称" /></FormItem>
      <FormItem label="供应商电话"><Input v-model:value="formState.supplierPhone" placeholder="供应商联系电话" /></FormItem>
      <div style="display:flex;gap:16px">
        <FormItem label="采购单价" style="flex:1"><InputNumber v-model:value="formState.unitPrice" :min="0" :step="0.01" placeholder="单价" style="width:100%" /></FormItem>
        <FormItem label="总金额" required style="flex:1"><InputNumber v-model:value="formState.totalAmount" :min="0.01" :step="0.01" placeholder="总金额" style="width:100%" /></FormItem>
      </div>
      <div style="display:flex;gap:16px">
        <FormItem label="采购日期" required style="flex:1"><DatePicker v-model:value="formState.purchaseDate" placeholder="选择日期" style="width:100%" /></FormItem>
        <FormItem label="预计到货" style="flex:1"><DatePicker v-model:value="formState.expectedArrivalDate" placeholder="预计到货日期" style="width:100%" /></FormItem>
      </div>
      <FormItem label="备注"><Input.TextArea v-model:value="formState.remark" placeholder="备注信息" :rows="3" /></FormItem>
      <FormItem :label="isEdit ? '合同附件（可选新增）' : '合同附件'" :required="!isEdit">
        <Upload v-model:file-list="fileList" list-type="picture-card" :multiple="true" :before-upload="() => false" @change="handleUpload" @remove="handleRemove">
          <div><PlusOutlined /><div style="margin-top:8px">选择文件</div></div>
        </Upload>
      </FormItem>
      <FormItem>
        <Button type="primary" :loading="submitting" @click="handleSubmit" style="margin-right:12px">{{ isEdit ? '保存修改' : '提交记录' }}</Button>
        <Button @click="handleCancel">取消</Button>
      </FormItem>
    </Form>
  </div>
</template>

<style scoped>
.po-form-page { padding: 24px; }
.po-form-page h2 { margin: 0 0 24px; font-size: 20px; }
</style>
