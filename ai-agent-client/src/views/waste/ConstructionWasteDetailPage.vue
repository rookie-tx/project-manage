<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useConstructionWasteStore } from '@/stores/constructionWaste'
import client from '@/api/client'
import { getFileUrl } from '@/api/fileUpload'
import { Descriptions, Image, Button, Spin, Alert } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const store = useConstructionWasteStore()

const record = ref(null)
const projects = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  const id = route.params.id
  try {
    const [wasteData, projRes] = await Promise.all([
      store.fetchById(id),
      client.get('/projects/select'),
    ])
    record.value = wasteData
    projects.value = projRes.data
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function projName(pid) {
  return projects.value.find(p => p.id === pid)?.name || '未知'
}

function formatDate(d) {
  if (!d) return '-'
  return String(d).substring(0, 10)
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>运出记录详情</h2>
      <Button @click="router.push('/construction-wastes')">返回列表</Button>
    </div>

    <Spin :spinning="loading">
      <Alert v-if="error" type="error" :message="error" style="margin-bottom:16px" />

      <template v-if="record">
        <Descriptions bordered :column="2" style="margin-bottom:24px">
          <Descriptions.Item label="所属项目">{{ projName(record.projectId) }}</Descriptions.Item>
          <Descriptions.Item label="废渣种类">{{ record.wasteType }}</Descriptions.Item>
          <Descriptions.Item label="数量">{{ record.quantity }} {{ record.unit }}</Descriptions.Item>
          <Descriptions.Item label="运出日期">{{ formatDate(record.removalDate) }}</Descriptions.Item>
          <Descriptions.Item label="车牌号">{{ record.vehiclePlate || '-' }}</Descriptions.Item>
          <Descriptions.Item label="司机姓名">{{ record.driverName || '-' }}</Descriptions.Item>
          <Descriptions.Item label="处置去向">{{ record.destination || '-' }}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{{ formatDate(record.createdAt) }}</Descriptions.Item>
          <Descriptions.Item label="备注" :span="2">{{ record.remark || '-' }}</Descriptions.Item>
        </Descriptions>

        <h3 style="margin-bottom:12px">现场照片</h3>
        <div v-if="record.files && record.files.length" style="display:flex;gap:8px;flex-wrap:wrap">
          <Image
            v-for="f in record.files"
            :key="f.id"
            :src="getFileUrl(f.id)"
            :alt="f.originalName"
            style="width:200px;height:200px;object-fit:cover;border-radius:4px"
          />
        </div>
        <div v-else style="color:#999">无照片</div>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
.page-container { background:#fff;padding:24px;border-radius:8px;min-height:400px }
.page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:24px }
.page-header h2 { margin:0;font-size:18px }
</style>
