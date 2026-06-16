<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import dayjs from 'dayjs'
import {
  Form,
  FormItem,
  Input,
  InputNumber,
  Select,
  DatePicker,
  Textarea,
  Button,
} from 'ant-design-vue'
import ProjectSelect from '@/components/common/ProjectSelect.vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'
import client from '@/api/client'

const props = defineProps({
  mode: {
    type: String,
    default: 'create',
  },
  initialValues: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['submit', 'cancel'])

const store = usePurchaseRequestStore()

const materials = ref([])
const selectedMaterial = ref(null)

const formState = reactive({
  projectId: null,
  materialId: null,
  materialSpec: '',
  materialUnitLabel: '',
  brand: '',
  quantity: null,
  arrivalDate: null,
  status: null,
  remark: '',
})

function toDayjs(val) {
  if (!val) return null
  return dayjs(val)
}

watch(
  () => props.initialValues,
  (vals) => {
    if (vals && Object.keys(vals).length > 0) {
      Object.assign(formState, {
        ...vals,
        materialId: vals.materialId ?? null,
        materialSpec: vals.materialSpec ?? '',
        materialUnitLabel: vals.materialUnit ?? '',
        arrivalDate: toDayjs(vals.arrivalDate),
      })
      if (vals.materialId) {
        selectedMaterial.value = materials.value.find(m => m.id === vals.materialId) || null
      }
    }
  },
  { immediate: true, deep: true },
)

const rules = {
  projectId: [{ required: true, message: '请选择项目' }],
  materialId: [{ required: true, message: '请选择物料' }],
  quantity: [{ required: true, message: '请输入数量' }],
  arrivalDate: [{ required: true, message: '请选择到场时间' }],
}

function handleMaterialChange(materialId) {
  formState.materialId = materialId
  const mat = materials.value.find(m => m.id === materialId)
  if (mat) {
    selectedMaterial.value = mat
    formState.materialSpec = mat.spec || ''
    formState.materialUnitLabel = store.getUnitLabel(mat.unit)
  } else {
    selectedMaterial.value = null
    formState.materialSpec = ''
    formState.materialUnitLabel = ''
  }
}

function buildSubmitData() {
  return {
    projectId: formState.projectId,
    materialId: formState.materialId,
    brand: formState.brand || undefined,
    quantity: formState.quantity,
    arrivalDate: formState.arrivalDate?.format?.('YYYY-MM-DD') || formState.arrivalDate,
    remark: formState.remark || undefined,
  }
}

function handleFinish() {
  emit('submit', buildSubmitData())
}

onMounted(async () => {
  try {
    const { data } = await client.get('/materials')
    materials.value = data || []
  } catch { /* ignore */ }
})
</script>

<template>
  <Form
    :model="formState"
    :rules="rules"
    label-col="{ span: 4 }"
    wrapper-col="{ span: 16 }"
    style="max-width: 640px"
    @finish="handleFinish"
  >
    <FormItem label="项目" name="projectId">
      <ProjectSelect v-model:value="formState.projectId" />
    </FormItem>

    <FormItem label="物料" name="materialId">
      <Select
        v-model:value="formState.materialId"
        placeholder="请选择物料"
        show-search
        :filter-option="(input, option) => option.label.includes(input)"
        :options="materials.map(m => ({ value: m.id, label: `${m.code} ${m.name}` }))"
        @change="handleMaterialChange"
      />
    </FormItem>

    <FormItem label="规格">
      <Input v-model:value="formState.materialSpec" disabled placeholder="选择物料后自动带出" />
    </FormItem>

    <FormItem label="单位">
      <Input v-model:value="formState.materialUnitLabel" disabled placeholder="选择物料后自动带出" />
    </FormItem>

    <FormItem label="品牌" name="brand">
      <Input v-model:value="formState.brand" placeholder="请输入品牌" />
    </FormItem>

    <FormItem label="数量" name="quantity">
      <InputNumber v-model:value="formState.quantity" :min="0" :precision="2" style="width: 100%" placeholder="请输入数量" />
    </FormItem>

    <FormItem label="到场时间" name="arrivalDate">
      <DatePicker v-model:value="formState.arrivalDate" style="width: 100%" />
    </FormItem>

    <FormItem v-if="mode === 'edit'" label="状态" name="status">
      <span>{{ store.getStatusLabel(props.initialValues.statusCode) }}</span>
    </FormItem>

    <FormItem label="备注" name="remark">
      <Textarea v-model:value="formState.remark" placeholder="请输入备注" :rows="3" />
    </FormItem>

    <FormItem :wrapper-col="{ offset: 4, span: 16 }">
      <Button type="primary" html-type="submit" :loading="store.loading" style="margin-right: 8px">
        {{ mode === 'create' ? '创建' : '保存' }}
      </Button>
      <Button @click="$emit('cancel')">取消</Button>
    </FormItem>
  </Form>
</template>
