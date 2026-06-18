<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Form, FormItem, Input, InputNumber, DatePicker, Button, Upload, Alert, Modal, message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'
import { uploadFiles } from '@/api/fileUpload'
import QuotationList from '@/components/purchase/QuotationList.vue'
import client from '@/api/client'
import dayjs from 'dayjs'

const router = useRouter(); const route = useRoute()
const store = usePurchaseOrderStore()

const submitting = ref(false); const error = ref(null)
const fileList = ref([])

// 关联的采购申请信息（只读展示）
const prInfo = ref(null)

const formState = reactive({
  supplierName: '', supplierPhone: '',
  unitPrice: null, totalAmount: null, purchaseDate: null,
  expectedArrivalDate: null, remark: '',
})

const isPending = computed(() => store.current?.statusCode === 0)

onMounted(async () => {
  try { await store.fetchDetail(route.params.id)
    const c = store.current
    if (c) {
      const pr = (c.purchaseRequests && c.purchaseRequests[0]) || {}
      prInfo.value = {
        code: pr.code || '-',
        name: pr.name || '-',
        materialName: pr.materialName || '-',
        materialSpec: pr.materialSpec || '-',
        quantityStr: pr.quantityStr || '-',
        projectName: pr.projectName || '-',
        arrivalDate: pr.arrivalDate || '-'
      }
      formState.supplierName = c.supplierName || ''
      formState.supplierPhone = c.supplierPhone || ''
      formState.unitPrice = c.unitPrice
      formState.totalAmount = c.totalAmount
      formState.purchaseDate = c.purchaseDate ? dayjs(c.purchaseDate) : null
      formState.expectedArrivalDate = c.expectedArrivalDate ? dayjs(c.expectedArrivalDate) : null
      formState.remark = c.remark || ''
      if (c.files && c.files.length > 0) {
        const API_BASE = client.defaults.baseURL?.replace(/\/api$/, '') || ''
        fileList.value = c.files.map(f => ({
          uid: String(f.id), name: f.originalName, status: 'done', url: API_BASE + f.url
        }))
      }
      if (isPending.value) {
        await store.fetchQuotations(route.params.id)
      }
    }
  } catch { /* ignore */ }
})

function handleBack() { router.back() }

function handleSubmit() {
  error.value = null
  if (!formState.supplierName.trim()) { error.value = '请输入供应商名称'; return }
  if (!formState.totalAmount || formState.totalAmount <= 0) { error.value = '请输入有效总金额'; return }
  if (!formState.purchaseDate) { error.value = '请选择采购日期'; return }

  Modal.confirm({
    title: '确认采购完成',
    content: '确认后采购单将流转为「已采购」状态，是否继续？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      submitting.value = true
      try {
        let fileIds = []
        if (fileList.value.length > 0) {
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
        if (fileIds.length > 0) {
          payload.fileIds = fileIds
        }

        await store.update(route.params.id, payload)
        await store.changeStatus(route.params.id, 1)
        message.success('采购完成')
        router.back()
      } catch (e) {
        error.value = e.message || '提交失败'
      } finally {
        submitting.value = false
      }
    }
  })
}

function handleUpload(info) { if (info.file.status === 'done') fileList.value.push(info.file) }
function handleRemove(file) { fileList.value = fileList.value.filter(f => f !== file) }

async function onQuotationSelected() {
  message.success('报价已选用，采购完成')
  router.back()
}
</script>

<template>
  <div class="po-form-page">
    <h2>采购详情</h2>
    <Alert v-if="error" type="error" :message="error" closable style="margin-bottom:16px" />

    <!-- 关联采购申请（只读展示） -->
    <div v-if="prInfo" class="pr-info-card">
      <div class="pr-info-title">关联采购申请</div>
      <div class="form-row">
        <div class="form-half">
          <div class="info-label">申请编号</div>
          <div class="info-value">{{ prInfo.code }}</div>
        </div>
        <div class="form-half">
          <div class="info-label">物料名称</div>
          <div class="info-value">{{ prInfo.materialName }}</div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-half">
          <div class="info-label">规格型号</div>
          <div class="info-value">{{ prInfo.materialSpec }}</div>
        </div>
        <div class="form-half">
          <div class="info-label">数量</div>
          <div class="info-value">{{ prInfo.quantityStr }}</div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-half">
          <div class="info-label">所属项目</div>
          <div class="info-value">{{ prInfo.projectName }}</div>
        </div>
        <div class="form-half">
          <div class="info-label">预计到货</div>
          <div class="info-value">{{ prInfo.arrivalDate }}</div>
        </div>
      </div>
    </div>

    <!-- 编辑表单 -->
    <Form layout="vertical" class="po-form">
      <div class="form-row">
        <FormItem label="供应商名称" required class="form-half"><Input v-model:value="formState.supplierName" placeholder="供应商公司名称" /></FormItem>
        <FormItem label="供应商电话" class="form-half"><Input v-model:value="formState.supplierPhone" placeholder="供应商联系电话" /></FormItem>
      </div>
      <div class="form-row">
        <FormItem label="采购单价" class="form-half"><InputNumber v-model:value="formState.unitPrice" :min="0" :step="0.01" placeholder="单价" style="width:100%" /></FormItem>
        <FormItem label="总金额" required class="form-half"><InputNumber v-model:value="formState.totalAmount" :min="0.01" :step="0.01" placeholder="总金额" style="width:100%" /></FormItem>
      </div>
      <div class="form-row">
        <FormItem label="采购日期" required class="form-half"><DatePicker v-model:value="formState.purchaseDate" placeholder="选择日期" style="width:100%" /></FormItem>
        <FormItem label="预计到货" class="form-half"><DatePicker v-model:value="formState.expectedArrivalDate" placeholder="预计到货日期" style="width:100%" /></FormItem>
      </div>
      <FormItem label="备注"><Input.TextArea v-model:value="formState.remark" placeholder="备注信息" :rows="3" /></FormItem>
      <FormItem label="合同附件（可选）">
        <Upload v-model:file-list="fileList" list-type="picture-card" :multiple="true" :before-upload="() => false" @change="handleUpload" @remove="handleRemove">
          <div><PlusOutlined /><div style="margin-top:8px">选择文件</div></div>
        </Upload>
      </FormItem>
      <FormItem>
        <Button type="primary" :loading="submitting" @click="handleSubmit" style="margin-right:12px">采购完成</Button>
        <Button @click="handleBack">返回</Button>
      </FormItem>
    </Form>

    <!-- 供应商报价 -->
    <QuotationList v-if="isPending" :order-id="route.params.id" :order-status="store.current?.statusCode" @selected="onQuotationSelected" />
  </div>
</template>

<style scoped>
.po-form-page { padding: 24px; }
.po-form-page h2 { margin: 0 0 24px; font-size: 20px; }

/* 采购申请信息卡片 & 表单：统一宽度，不铺满 */
.pr-info-card, .po-form {
  max-width: 800px;
}
.pr-info-card { background: #fafafa; border: 1px solid #f0f0f0; border-radius: 8px; padding: 20px 24px; margin-bottom: 24px; }
.pr-info-title { font-size: 14px; font-weight: 600; color: #1a1a1a; margin-bottom: 16px; }

.info-label { font-size: 12px; color: #999; margin-bottom: 4px; }
.info-value { font-size: 14px; color: #333; }

/* 表单布局 */
.form-row { display: flex; gap: 24px; }
.form-half { flex: 1; }

/* 报价表格不限制宽度 */
.quotation-list { max-width: none; }
</style>
