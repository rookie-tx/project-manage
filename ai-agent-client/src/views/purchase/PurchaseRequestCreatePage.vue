<script setup>
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PurchaseRequestForm from '@/components/purchase/PurchaseRequestForm.vue'
import { usePurchaseRequestStore } from '@/stores/purchaseRequest'

const router = useRouter()
const store = usePurchaseRequestStore()

async function handleSubmit(formData) {
  try {
    await store.create(formData)
    message.success('采购申请创建成功')
    router.push('/procurement')
  } catch {
    message.error(store.error || '创建失败')
  }
}

function handleCancel() {
  router.push('/procurement')
}
</script>

<template>
  <div class="pr-create-page">
    <h2>新建采购申请</h2>
    <PurchaseRequestForm mode="create" @submit="handleSubmit" @cancel="handleCancel" />
  </div>
</template>

<style scoped>
.pr-create-page {
  padding: 24px;
}

.pr-create-page h2 {
  margin: 0 0 24px;
  font-size: 20px;
}
</style>
