<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Descriptions, Button, Space, Spin, Modal, Select, message } from 'ant-design-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'
import client from '@/api/client'

const router = useRouter(); const route = useRoute(); const store = usePurchaseOrderStore()
const nextStatus = ref(null)
const apiOrigin = computed(() => client.defaults.baseURL?.replace(/\/api$/, '') || '')

onMounted(async () => { try { await store.fetchDetail(route.params.id) } catch { /* */ } })

function handleEdit() { router.push(`/purchase-orders/${route.params.id}/edit`) }
function handleDelete() {
  Modal.confirm({ title: '确认删除', content: '确定要删除此采购记录吗？', okText: '确认删除', okType: 'danger', cancelText: '取消',
    onOk: async () => { try { await store.remove(route.params.id); message.success('已删除'); router.back() } catch { message.error(store.error || '删除失败') } } })
}

async function handleStatusChange() {
  if (nextStatus.value == null) return
  try { await store.changeStatus(route.params.id, nextStatus.value); message.success('状态已更新'); nextStatus.value = null; await store.fetchDetail(route.params.id) }
  catch { message.error(store.error || '操作失败') }
}

function formatDate(d) { return d ? String(d).substring(0, 10) : '-' }
function isEditing() { return store.current?.statusCode === 0 }
</script>

<template>
  <div class="po-detail-page">
    <Spin :spinning="store.loading">
      <template v-if="store.error"><p style="color:#ff4d4f">{{ store.error }}</p><Button @click="router.back()">返回</Button></template>
      <template v-else-if="store.current">
        <div class="detail-header">
          <h2>{{ store.current.purchaseRequestName || '采购记录' }}</h2>
          <Space>
            <Button v-if="isEditing()" @click="handleEdit">编辑</Button>
            <Button v-if="isEditing()" danger @click="handleDelete">删除</Button>
            <Button @click="router.back()">返回</Button>
          </Space>
        </div>
        <Descriptions bordered :column="2" style="margin-top:16px">
          <Descriptions.Item label="供应商">{{ store.current.supplierName }}</Descriptions.Item>
          <Descriptions.Item label="供应商电话">{{ store.current.supplierPhone || '-' }}</Descriptions.Item>
          <Descriptions.Item label="采购单价">{{ store.current.unitPrice != null ? '¥' + store.current.unitPrice : '-' }}</Descriptions.Item>
          <Descriptions.Item label="总金额">¥{{ store.current.totalAmount }}</Descriptions.Item>
          <Descriptions.Item label="采购日期">{{ formatDate(store.current.purchaseDate) }}</Descriptions.Item>
          <Descriptions.Item label="预计到货">{{ formatDate(store.current.expectedArrivalDate) }}</Descriptions.Item>
          <Descriptions.Item label="状态"><a-tag :color="['orange','blue','green'][store.current.statusCode]">{{ store.getStatusLabel(store.current.statusCode) }}</a-tag></Descriptions.Item>
          <Descriptions.Item label="创建人">{{ store.current.createdByName || '-' }}</Descriptions.Item>
          <Descriptions.Item label="备注" :span="2">{{ store.current.remark || '-' }}</Descriptions.Item>
        </Descriptions>

        <div v-if="store.current.purchaseRequests?.length" style="margin-top:24px">
          <h3 style="margin-bottom:12px">关联采购申请</h3>
          <div v-for="pr in store.current.purchaseRequests" :key="pr.id" style="padding:10px 16px;margin-bottom:8px;background:#f9fafb;border-radius:8px;display:flex;gap:16px;flex-wrap:wrap;font-size:13px">
            <span style="color:#6366f1;font-weight:600">{{ pr.code || '-' }}</span>
            <span>{{ pr.name }}</span>
            <span style="color:#9ca3af">{{ pr.projectName }}</span>
            <span style="color:#9ca3af">{{ pr.specification || '-' }}</span>
            <span>{{ pr.quantityStr }}</span>
            <span style="color:#9ca3af">{{ pr.arrivalDate || '-' }}</span>
          </div>
        </div>

        <div v-if="store.current.files?.length" style="margin-top:24px">
          <h3 style="margin-bottom:12px">附件</h3>
          <Space wrap>
            <a v-for="f in store.current.files" :key="f.id" :href="apiOrigin + f.url" target="_blank" style="padding:6px 12px;background:#f3f4f6;border-radius:8px;color:#6366f1">{{ f.originalName }}</a>
          </Space>
        </div>

        <div v-if="store.current.statusCode < 2" style="margin-top:24px;padding:16px;background:#f9fafb;border-radius:12px">
          <Space>
            <span>状态流转：</span>
            <Select v-model:value="nextStatus" placeholder="选择目标状态" style="width:140px"
              :options="store.current.statusCode === 0 ? [{value:1,label:'已采购'}] : [{value:2,label:'已到货'}]" />
            <Button type="primary" @click="handleStatusChange">确认变更</Button>
          </Space>
        </div>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.po-detail-page { padding: 24px; }
.detail-header { display: flex; justify-content: space-between; align-items: center; }
.detail-header h2 { margin: 0; font-size: 20px; }
</style>
