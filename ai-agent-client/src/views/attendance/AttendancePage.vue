<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Button, Table, Tag, message, Card } from 'ant-design-vue'
import client from '@/api/client'

const today = ref(null)
const records = ref([])
const loading = ref(false)
const currentMonth = ref(new Date().toISOString().substring(0, 7))

const isClockedIn = computed(() => today.value && today.value.status === 'CLOCKED_IN')
const isClockedOut = computed(() => today.value && today.value.status === 'CLOCKED_OUT')

onMounted(async () => { await fetchToday(); fetchRecords() })

async function fetchToday() {
  try { const { data } = await client.get(`/attendance/my?yearMonth=${currentMonth.value}`); const arr = data || []; if (arr.length > 0 && arr[0].clockIn?.startsWith(new Date().toISOString().substring(0,10))) today.value = arr[0]; else today.value = null } catch { today.value = null }
}
async function fetchRecords() { loading.value = true; try { const { data } = await client.get(`/attendance/my?yearMonth=${currentMonth.value}`); records.value = data || [] } finally { loading.value = false } }

async function clockIn() {
  try { const { data } = await client.post('/attendance/clock-in'); today.value = data; message.success('上班打卡成功'); fetchRecords() } catch (e) { message.error(e.message) }
}
async function clockOut() {
  try { const { data } = await client.post('/attendance/clock-out'); today.value = data; message.success('下班打卡成功'); fetchRecords() } catch (e) { message.error(e.message) }
}

function formatTime(d) { if (!d) return '--:--'; return d.substring(11, 16) }
function statusText(s) { return s === 'CLOCKED_IN' ? '已上班' : '已下班' }
function formatDate(d) { if (!d) return ''; return d.substring(0, 10) }

const columns = [
  { title: '日期', dataIndex: 'clockIn', key: 'date', width: 110 },
  { title: '上班', key: 'in', width: 80 },
  { title: '下班', key: 'out', width: 80 },
  { title: '状态', key: 'status', width: 80 },
]
</script>

<template>
  <div class="attendance-page">
    <Card style="margin-bottom:24px">
      <div style="display:flex;justify-content:center;gap:24px;align-items:center">
        <Button type="primary" size="large" :disabled="isClockedOut" @click="clockIn" style="width:140px;height:56px;font-size:18px">
          {{ isClockedIn ? '已打卡上班' : '上班打卡' }}
        </Button>
        <Button size="large" :disabled="!isClockedIn || isClockedOut" @click="clockOut" style="width:140px;height:56px;font-size:18px">
          {{ isClockedOut ? '已下班' : '下班打卡' }}
        </Button>
      </div>
      <div style="text-align:center;margin-top:16px;color:#666">
        今日：{{ today ? formatTime(today.clockIn) : '--:--' }} 上班 | {{ today?.clockOut ? formatTime(today.clockOut) : '--:--' }} 下班
      </div>
    </Card>

    <h3>本月记录</h3>
    <Table :columns="columns" :data-source="records" :loading="loading" row-key="id" size="small" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'date'">{{ formatDate(record.clockIn) }}</template>
        <template v-if="column.key === 'in'">{{ formatTime(record.clockIn) }}</template>
        <template v-if="column.key === 'out'">{{ formatTime(record.clockOut) }}</template>
        <template v-if="column.key === 'status'"><Tag :color="record.status==='CLOCKED_OUT'?'green':'orange'">{{ statusText(record.status) }}</Tag></template>
      </template>
    </Table>
  </div>
</template>

<style scoped>.attendance-page { max-width:640px;margin:0 auto;padding:24px } h3 { margin:0 0 12px;font-size:16px }</style>
