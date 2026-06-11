<script setup>
import { onMounted, ref } from 'vue'
import { Timeline, Spin, Empty } from 'ant-design-vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'

const props = defineProps({
  purchaseRequestId: { type: [String, Number], required: true },
})

const store = usePurchaseRequestStore()
const history = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    history.value = await store.fetchApprovalHistory(props.purchaseRequestId)
  } catch {
    // error in store
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="approval-history">
    <h3 style="margin-bottom: 16px">审批历史</h3>
    <Spin :spinning="loading">
      <Empty v-if="!loading && history.length === 0" description="暂无审批记录" />
      <Timeline v-else>
        <Timeline.Item
          v-for="(item, index) in history"
          :key="index"
          :color="item.result === '通过' ? 'green' : 'red'"
        >
          <div>
            <div style="font-weight: 500">{{ item.nodeName }}</div>
            <div>审批人：{{ item.assigneeName || item.assignee || '-' }}</div>
            <div>结果：{{ item.result }}</div>
            <div v-if="item.comment">意见：{{ item.comment }}</div>
            <div style="color: #999; font-size: 12px">{{ item.time || '-' }}</div>
          </div>
        </Timeline.Item>
      </Timeline>
    </Spin>
  </div>
</template>

<style scoped>
.approval-history {
  margin-top: 24px;
}
</style>
