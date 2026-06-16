<script setup>
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Descriptions, Button, Space, Spin, Modal, message } from 'ant-design-vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'
import ApprovalHistory from '@/components/approval/ApprovalHistory.vue'
import SubmitApprovalDialog from '@/components/approval/SubmitApprovalDialog.vue'

const router = useRouter()
const route = useRoute()
const store = usePurchaseRequestStore()
const submitDialogVisible = ref(false)

onMounted(async () => {
  try {
    await store.fetchDetail(route.params.id)
    store.initUnitDict()
  } catch {
    // error is in store
  }
})

function handleEdit() {
  router.push(`/procurement/${route.params.id}/edit`)
}

function handleDelete() {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据不可恢复，确定要删除此采购申请吗？',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await store.remove(route.params.id)
        message.success('删除成功')
        router.back()
      } catch {
        message.error(store.error || '删除失败')
      }
    },
  })
}

async function handleSubmitDialogClose(refresh) {
  submitDialogVisible.value = false
  if (refresh) {
    try {
      await store.fetchDetail(route.params.id)
    } catch {
      // ignore
    }
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr
}

function isPending() {
  return store.current?.statusCode === 0
}
</script>

<template>
  <div class="pr-detail-page">
    <Spin :spinning="store.loading">
      <template v-if="store.error">
        <p class="detail-error">{{ store.error }}</p>
        <Button @click="router.back()">返回</Button>
      </template>

      <template v-else-if="store.current">
        <div class="detail-header">
          <h2>{{ store.current.name }}</h2>
          <Space>
            <template v-if="isPending()">
              <Button type="primary" @click="submitDialogVisible = true">提交审批</Button>
              <Button @click="handleEdit">编辑</Button>
              <Button danger @click="handleDelete">删除</Button>
            </template>
            <Button @click="router.back()">返回</Button>
          </Space>
        </div>

        <Descriptions bordered :column="2" style="margin-top: 16px">
          <Descriptions.Item label="编号">{{ store.current.code || '-' }}</Descriptions.Item>
          <Descriptions.Item label="项目">{{ store.current.projectName || '-' }}</Descriptions.Item>
          <Descriptions.Item label="物料名称">{{ store.current.name }}</Descriptions.Item>
          <Descriptions.Item label="规格">{{ store.current.specification || '-' }}</Descriptions.Item>
          <Descriptions.Item label="品牌">{{ store.current.brand || '-' }}</Descriptions.Item>
          <Descriptions.Item label="数量">{{ store.current.quantity }}</Descriptions.Item>
          <Descriptions.Item label="单位">
            {{ store.getUnitLabel(store.current.unitCode) }}
          </Descriptions.Item>
          <Descriptions.Item label="到场时间">{{ formatDate(store.current.arrivalDate) }}</Descriptions.Item>
          <Descriptions.Item label="状态">
            {{ store.getStatusLabel(store.current.statusCode) }}
          </Descriptions.Item>
          <Descriptions.Item v-if="store.current.currentApprovalNode" label="当前审批节点">
            {{ store.current.currentApprovalNode }}
          </Descriptions.Item>
          <Descriptions.Item label="创建人">{{ store.current.createdByName || '-' }}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{{ formatDate(store.current.createdAt) }}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{{ formatDate(store.current.updatedAt) }}</Descriptions.Item>
          <Descriptions.Item label="备注" :span="2">{{ store.current.remark || '-' }}</Descriptions.Item>
        </Descriptions>

        <ApprovalHistory :purchase-request-id="route.params.id" />

        <SubmitApprovalDialog
          v-if="submitDialogVisible"
          :visible="submitDialogVisible"
          :purchase-request-id="route.params.id"
          @close="handleSubmitDialogClose"
        />
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.pr-detail-page {
  padding: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header h2 {
  margin: 0;
  font-size: 20px;
}

.detail-error {
  color: #ff4d4f;
  font-size: 16px;
  margin-bottom: 16px;
}
</style>
