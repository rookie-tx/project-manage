<script setup>
import { reactive, watch } from 'vue'
import dayjs from 'dayjs'
import {
  Form,
  FormItem,
  Input,
  Select,
  DatePicker,
  Textarea,
  Button,
} from 'ant-design-vue'
import UserSelect from '@/components/common/UserSelect.vue'
import { useProjectStore } from '@/stores/project'

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

const projectStore = useProjectStore()

const formState = reactive({
  name: '',
  type: null,
  location: '',
  clientName: '',
  supervisorUnit: '',
  projectManagerId: null,
  plannedStartDate: null,
  plannedEndDate: null,
  actualStartDate: null,
  actualEndDate: null,
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
        plannedStartDate: toDayjs(vals.plannedStartDate),
        plannedEndDate: toDayjs(vals.plannedEndDate),
        actualStartDate: toDayjs(vals.actualStartDate),
        actualEndDate: toDayjs(vals.actualEndDate),
      })
    }
  },
  { immediate: true, deep: true },
)

const rules = {
  name: [{ required: true, message: '请输入项目名称' }],
  type: [{ required: true, message: '请选择项目类型' }],
  projectManagerId: [{ required: true, message: '请选择项目经理' }],
  plannedStartDate: [{ required: true, message: '请选择计划开工日期' }],
  plannedEndDate: [{ required: true, message: '请选择计划竣工日期' }],
}

function buildSubmitData() {
  const data = {
    name: formState.name,
    type: formState.type,
    location: formState.location || undefined,
    clientName: formState.clientName || undefined,
    supervisorUnit: formState.supervisorUnit || undefined,
    projectManagerId: formState.projectManagerId,
    plannedStartDate: formState.plannedStartDate?.format?.('YYYY-MM-DD') || formState.plannedStartDate,
    plannedEndDate: formState.plannedEndDate?.format?.('YYYY-MM-DD') || formState.plannedEndDate,
    remark: formState.remark || undefined,
  }
  if (props.mode === 'edit') {
    data.actualStartDate = formState.actualStartDate?.format?.('YYYY-MM-DD') || formState.actualStartDate || undefined
    data.actualEndDate = formState.actualEndDate?.format?.('YYYY-MM-DD') || formState.actualEndDate || undefined
    if (formState.status !== null && formState.status !== undefined) {
      data.status = formState.status
    }
  }
  return data
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
    <FormItem label="项目名称" name="name">
      <Input v-model:value="formState.name" placeholder="请输入项目名称" />
    </FormItem>

    <FormItem label="项目类型" name="type">
      <Select
        v-model:value="formState.type"
        placeholder="请选择项目类型"
        :options="projectStore.getTypeOptions()"
      />
    </FormItem>

    <FormItem label="项目地点" name="location">
      <Input v-model:value="formState.location" placeholder="请输入项目地点" />
    </FormItem>

    <FormItem label="甲方名称" name="clientName">
      <Input v-model:value="formState.clientName" placeholder="请输入甲方名称" />
    </FormItem>

    <FormItem label="监理单位" name="supervisorUnit">
      <Input v-model:value="formState.supervisorUnit" placeholder="请输入监理单位" />
    </FormItem>

    <FormItem label="项目经理" name="projectManagerId">
      <UserSelect v-model:value="formState.projectManagerId" />
    </FormItem>

    <FormItem label="计划开工日期" name="plannedStartDate">
      <DatePicker v-model:value="formState.plannedStartDate" style="width: 100%" />
    </FormItem>

    <FormItem label="计划竣工日期" name="plannedEndDate">
      <DatePicker v-model:value="formState.plannedEndDate" style="width: 100%" />
    </FormItem>

    <template v-if="mode === 'edit'">
      <FormItem label="实际开工日期" name="actualStartDate">
        <DatePicker v-model:value="formState.actualStartDate" style="width: 100%" />
      </FormItem>

      <FormItem label="实际竣工日期" name="actualEndDate">
        <DatePicker v-model:value="formState.actualEndDate" style="width: 100%" />
      </FormItem>

      <FormItem label="项目状态" name="status">
        <Select
          v-model:value="formState.status"
          placeholder="留空则不修改状态"
          allow-clear
          :options="projectStore.getStatusOptions()"
        />
      </FormItem>
    </template>

    <FormItem label="备注" name="remark">
      <Textarea v-model:value="formState.remark" placeholder="请输入备注" :rows="3" />
    </FormItem>

    <FormItem :wrapper-col="{ offset: 4, span: 16 }">
      <Button type="primary" html-type="submit" :loading="projectStore.loading" style="margin-right: 8px">
        {{ mode === 'create' ? '创建' : '保存' }}
      </Button>
      <Button @click="$emit('cancel')">取消</Button>
    </FormItem>
  </Form>
</template>
