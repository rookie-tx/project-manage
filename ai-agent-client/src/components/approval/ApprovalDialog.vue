<script setup>
import { ref } from 'vue'
import { Modal, Descriptions, Input, Space, Button, message } from 'ant-design-vue'
import { useApprovalStore } from '@/stores/approval'

const props = defineProps({
  visible: { type: Boolean, required: true },
  task: { type: Object, required: true },
})

const emit = defineEmits(['close'])

const approval = useApprovalStore()
const comment = ref('')
const submitting = ref(false)

async function handleApprove() {
  submitting.value = true
  try {
    await approval.completeTask(props.task.taskId, true, comment.value)
    message.success('审批通过')
    emit('close', true)
  } catch {
    message.error(approval.error || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleReject() {
  submitting.value = true
  try {
    await approval.completeTask(props.task.taskId, false, comment.value)
    message.success('已驳回')
    emit('close', true)
  } catch {
    message.error(approval.error || '操作失败')
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
    title="审批处理"
    width="600px"
    :footer="null"
    @cancel="handleCancel"
  >
    <Descriptions bordered :column="2" style="margin-bottom: 16px">
      <Descriptions.Item label="业务类型">{{ task.businessType || '-' }}</Descriptions.Item>
      <Descriptions.Item label="业务标题">{{ task.businessTitle || '-' }}</Descriptions.Item>
      <Descriptions.Item label="业务副标题">{{ task.businessSubtitle || '-' }}</Descriptions.Item>
      <Descriptions.Item label="当前节点">{{ task.nodeName || '-' }}</Descriptions.Item>
      <Descriptions.Item label="到达时间">{{ task.createTime || '-' }}</Descriptions.Item>
    </Descriptions>

    <div style="margin-bottom: 16px">
      <label style="display: block; margin-bottom: 8px; font-weight: 500">审批意见</label>
      <Input.TextArea
        v-model:value="comment"
        :rows="3"
        placeholder="请输入审批意见（选填）"
      />
    </div>

    <div style="display: flex; justify-content: flex-end">
      <Space>
        <Button @click="handleCancel">取消</Button>
        <Button danger :loading="submitting" @click="handleReject">驳回</Button>
        <Button type="primary" :loading="submitting" @click="handleApprove">通过</Button>
      </Space>
    </div>
  </Modal>
</template>
