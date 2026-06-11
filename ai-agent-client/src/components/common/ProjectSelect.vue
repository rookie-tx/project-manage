<script setup>
import { ref, onMounted } from 'vue'
import { Select } from 'ant-design-vue'
import client from '@/api/client'

defineProps({
  modelValue: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue'])

const projects = ref([])
const loading = ref(false)

async function fetchProjects() {
  loading.value = true
  try {
    const { data } = await client.get('/projects/select')
    projects.value = data
  } finally {
    loading.value = false
  }
}

function handleChange(value) {
  emit('update:modelValue', value)
}

onMounted(() => {
  fetchProjects()
})
</script>

<template>
  <Select
    :value="modelValue"
    :loading="loading"
    placeholder="请选择项目"
    show-search
    option-filter-prop="label"
    @change="handleChange"
  >
    <Select.Option
      v-for="p in projects"
      :key="p.id"
      :value="p.id"
      :label="p.code + ' ' + p.name"
    >
      {{ p.code }} {{ p.name }}
    </Select.Option>
  </Select>
</template>
