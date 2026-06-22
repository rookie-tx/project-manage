<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { usePrint } from '@/composables/usePrint'
import { Table, Button, Spin } from 'ant-design-vue'
import { PrinterOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import client from '@/api/client'

const route = useRoute()
const projectStore = useProjectStore()
const { openPrint } = usePrint()

const projectId = route.params.id
const loading = ref(true)
const statsData = ref([])

const columns = [
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName' },
  { title: '规格', dataIndex: 'materialSpec', key: 'materialSpec' },
  { title: '单位', dataIndex: 'materialUnit', key: 'materialUnit' },
  { title: '总采购数量', dataIndex: 'totalQuantity', key: 'totalQuantity' },
  { title: '总采购金额', dataIndex: 'totalAmount', key: 'totalAmount' },
]

onMounted(async () => {
  try {
    await projectStore.fetchDetail(Number(projectId))
    const { data } = await client.get(`/projects/${projectId}/purchase-statistics`)
    statsData.value = data || []
  } finally {
    loading.value = false
  }
})

function handlePrint() {
  openPrint({
    title: `${projectStore.current?.name || ''} — 物料采购统计`,
    columns,
    data: statsData.value,
  })
}

function goBack() {
  window.history.back()
}
</script>

<template>
  <div class="stats-page">
    <div class="stats-header">
      <div>
        <Button type="link" @click="goBack"><ArrowLeftOutlined /> 返回</Button>
        <h2>{{ projectStore.current?.name || '加载中...' }} — 物料采购统计</h2>
      </div>
      <Button type="primary" @click="handlePrint" :disabled="!statsData.length">
        <PrinterOutlined /> 打印
      </Button>
    </div>

    <Spin :spinning="loading">
      <Table
        :columns="columns"
        :data-source="statsData"
        :loading="loading"
        row-key="materialId"
        :pagination="false"
        style="background:#fff"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            {{ record.totalAmount != null ? '¥' + Number(record.totalAmount).toLocaleString() : '-' }}
          </template>
        </template>
      </Table>
    </Spin>
  </div>
</template>

<style scoped>
.stats-page { padding: 24px; }
.stats-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.stats-header h2 { margin: 8px 0 0 0; font-size: 18px; }
</style>
