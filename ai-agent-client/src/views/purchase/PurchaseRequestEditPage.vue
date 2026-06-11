<script setup>
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Spin, Button, message } from 'ant-design-vue'
import PurchaseRequestForm from '@/components/purchase/PurchaseRequestForm.vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'

const router = useRouter()
const route = useRoute()
const store = usePurchaseRequestStore()

onMounted(async () => {
  try {
    await store.fetchDetail(route.params.id)
    if (store.current && store.current.statusCode !== 0) {
      message.warning('当前状态不允许编辑')
      router.replace(`/procurement/${route.params.id}`)
    }
  } catch {
    // error is in store
  }
})

const initialValues = computed(() => store.current || {})

async function handleSubmit(formData) {
  try {
    await store.update(route.params.id, formData)
    message.success('采购申请更新成功')
    router.back()
  } catch {
    message.error(store.error || '更新失败')
  }
}

function handleCancel() {
  router.back()
}
</script>

<template>
  <div class="pr-edit-page">
    <Spin :spinning="store.loading && !store.current">
      <template v-if="store.error">
        <p class="edit-error">{{ store.error }}</p>
        <Button @click="router.push('/procurement')">返回列表</Button>
      </template>

      <template v-else-if="store.current">
        <h2>编辑采购申请</h2>
        <PurchaseRequestForm
          mode="edit"
          :initial-values="initialValues"
          @submit="handleSubmit"
          @cancel="handleCancel"
        />
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.pr-edit-page {
  padding: 24px;
}

.pr-edit-page h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.edit-error {
  color: #ff4d4f;
  font-size: 16px;
  margin-bottom: 16px;
}
</style>
