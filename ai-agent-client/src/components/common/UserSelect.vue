<script setup>
import { onMounted } from 'vue'
import { Select } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'

defineProps({
  modelValue: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()

onMounted(() => {
  userStore.fetchUsers()
})

function handleChange(value) {
  emit('update:modelValue', value)
}
</script>

<template>
  <Select
    :value="modelValue"
    :loading="userStore.loading"
    placeholder="请选择项目经理"
    show-search
    option-filter-prop="label"
    @change="handleChange"
  >
    <Select.Option
      v-for="u in userStore.users"
      :key="u.id"
      :value="u.id"
      :label="u.username"
    >
      {{ u.username }}
    </Select.Option>
  </Select>
</template>
