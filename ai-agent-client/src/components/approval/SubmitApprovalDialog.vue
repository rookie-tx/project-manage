<script setup>
import { ref, onMounted } from 'vue'
import { Modal, Select, Space, Button, message } from 'ant-design-vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'
import client from '@/api/client'

const props = defineProps({
  visible: { type: Boolean, required: true },
  purchaseRequestId: { type: [String, Number], required: true },
})

const emit = defineEmits(['close'])

const store = usePurchaseRequestStore()
const users = ref([])
const pmUserId = ref(null)
const submitting = ref(false)

onMounted(async () => {
  try {
    const { data } = await client.get('/users/select')
    users.value = Array.isArray(data) ? data : []
  } catch {
    // ignore
  }
})

async function handleSubmit() {
  if (!pmUserId.value) {
    message.warning('请选择审批人')
    return
  }
  submitting.value = true
  try {
    await store.submitApproval(props.purchaseRequestId, {
      pmUserId: pmUserId.value,
    })
    message.success('提交审批成功')
    emit('close', true)
  } catch {
    message.error(store.error || '提交失败')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  emit('close', false)
}
</script>

<template>
  <Modal
    :open="visible"
    title="提交审批"
    width="400px"
    :footer="null"
    @cancel="handleCancel"
  >
    <div style="margin-bottom: 16px">
      <label style="display: block; margin-bottom: 8px; font-weight: 500"><span style="color: #ef4444">*</span> 项目经理审批</label>
      <Select
        v-model:value="pmUserId"
        placeholder="请选择项目经理"
        show-search
        option-filter-prop="label"
        style="width: 100%"
        :options="users.map(u => ({ value: u.id, label: u.displayName || u.username }))"
      />
    </div>

    <div style="display: flex; justify-content: flex-end">
      <Space>
        <Button @click="handleCancel">取消</Button>
        <Button type="primary" :loading="submitting" @click="handleSubmit">确认提交</Button>
      </Space>
    </div>
  </Modal>
</template>
