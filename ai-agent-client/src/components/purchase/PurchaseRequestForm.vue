<script setup>
import { reactive, watch } from 'vue'
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

const formState = reactive({
  projectId: null,
  name: '',
  specification: '',
  brand: '',
  quantity: null,
  unit: null,
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
        unit: vals.unitCode ?? null,
        arrivalDate: toDayjs(vals.arrivalDate),
      })
    }
  },
  { immediate: true, deep: true },
)

const rules = {
  projectId: [{ required: true, message: '请选择项目' }],
  name: [{ required: true, message: '请输入物料名称' }],
  quantity: [{ required: true, message: '请输入数量' }],
  unit: [{ required: true, message: '请选择单位' }],
  arrivalDate: [{ required: true, message: '请选择到场时间' }],
}

function buildSubmitData() {
  return {
    projectId: formState.projectId,
    name: formState.name,
    specification: formState.specification || undefined,
    brand: formState.brand || undefined,
    quantity: formState.quantity,
    unit: formState.unit,
    arrivalDate: formState.arrivalDate?.format?.('YYYY-MM-DD') || formState.arrivalDate,
    remark: formState.remark || undefined,
  }
}

function handleFinish() {
  emit('submit', buildSubmitData())
}
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

    <FormItem label="物料名称" name="name">
      <Input v-model:value="formState.name" placeholder="请输入物料名称" />
    </FormItem>

    <FormItem label="规格" name="specification">
      <Input v-model:value="formState.specification" placeholder="请输入规格" />
    </FormItem>

    <FormItem label="品牌" name="brand">
      <Input v-model:value="formState.brand" placeholder="请输入品牌" />
    </FormItem>

    <FormItem label="数量" name="quantity">
      <InputNumber v-model:value="formState.quantity" :min="0" :precision="2" style="width: 100%" placeholder="请输入数量" />
    </FormItem>

    <FormItem label="单位" name="unit">
      <Select
        v-model:value="formState.unit"
        placeholder="请选择单位"
        :options="store.getUnitOptions()"
      />
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
