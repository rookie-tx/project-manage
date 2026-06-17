import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { usePurchaseOrderStore } from '@/stores/purchaseOrder'
import { useAuthStore } from '@/stores/auth'
import client from '@/api/client'
import dayjs from 'dayjs'

/**
 * 采购单列表共享逻辑
 * @param {number|null} statusFilter - null=汇总, 0=待采购, 1=已采购, 2=已到货
 */
export function usePurchaseOrderList(statusFilter) {
  const store = usePurchaseOrderStore()
  const auth = useAuthStore()

  const pagination = reactive({ current: 1, pageSize: 20 })

  const columns = [
    { title: '采购申请编号', key: 'code', width: 140 },
    { title: '名称', key: 'name', width: 160 },
    { title: '物料', key: 'material', width: 140 },
    { title: '规格', key: 'spec', width: 120 },
    { title: '数量', key: 'qty', width: 80 },
    { title: '供应商', key: 'supplier', width: 80 },
    { title: '单价', key: 'unitPrice', width: 100 },
    { title: '总金额', key: 'totalAmount', width: 100 },
    { title: '采购日期', key: 'purchaseDate', width: 110 },
    { title: '预计到货', key: 'arrivalDate', width: 110 },
    { title: '状态', key: 'status', width: 80 },
    { title: '操作', key: 'action', width: 120 },
  ]

  // --- action visibility ---
  const showEditAction = computed(() => statusFilter === 0)
  const showInboundAction = computed(() => statusFilter === 1)
  const showStatusColumn = computed(() => statusFilter === null)

  // --- edit modal ---
  const editVisible = ref(false)
  const editingPO = ref(null)
  const editForm = reactive({
    supplierName: '', supplierPhone: '', unitPrice: null, totalAmount: null,
    purchaseDate: null, expectedArrivalDate: null, remark: '',
  })
  const editError = ref(null)
  const editSubmitting = ref(false)

  // --- inbound modal ---
  const inboundVisible = ref(false)
  const inboundPO = ref(null)
  const warehouses = ref([])
  const inboundForm = reactive({
    warehouseId: null, materialId: null, materialLabel: '', materialSpec: '', quantity: 1,
  })
  const inboundError = ref(null)
  const inboundSubmitting = ref(false)

  // --- helpers ---
  function prField(po, field) {
    const p = po?.purchaseRequests?.[0]
    if (!p) return '-'
    return p[field] || '-'
  }
  function formatDate(d) { return d ? String(d).substring(0, 10) : '-' }
  function formatMoney(v) { return v != null ? '¥' + Number(v).toLocaleString() : '-' }

  // --- API params ---
  function buildParams() {
    const params = { page: pagination.current, size: pagination.pageSize }
    if (statusFilter === 0) {
      params.tab = 'pending'
    } else if (statusFilter !== null) {
      params.status = statusFilter
    }
    return params
  }

  // --- load ---
  onMounted(() => loadData())

  async function loadData() {
    await store.fetchList(buildParams())
  }

  async function handleTableChange(pag) {
    pagination.current = pag.current
    await loadData()
  }

  // --- edit ---
  function openEdit(record) {
    editingPO.value = record
    Object.assign(editForm, {
      supplierName: record.supplierName || '',
      supplierPhone: record.supplierPhone || '',
      unitPrice: record.unitPrice,
      totalAmount: record.totalAmount,
      purchaseDate: record.purchaseDate ? dayjs(record.purchaseDate) : null,
      expectedArrivalDate: record.expectedArrivalDate ? dayjs(record.expectedArrivalDate) : null,
      remark: record.remark || '',
    })
    editError.value = null
    editVisible.value = true
  }

  async function handleEdit() {
    editError.value = null
    editSubmitting.value = true
    try {
      const payload = {
        supplierName: editForm.supplierName,
        supplierPhone: editForm.supplierPhone,
        unitPrice: editForm.unitPrice,
        totalAmount: editForm.totalAmount,
        purchaseDate: editForm.purchaseDate ? dayjs(editForm.purchaseDate).format('YYYY-MM-DD') : null,
        expectedArrivalDate: editForm.expectedArrivalDate ? dayjs(editForm.expectedArrivalDate).format('YYYY-MM-DD') : null,
        remark: editForm.remark,
      }
      await store.update(editingPO.value.id, payload)
      await store.changeStatus(editingPO.value.id, 1)
      editVisible.value = false
      await loadData()
      message.success('采购完成')
    } catch (e) {
      editError.value = e.message
    } finally {
      editSubmitting.value = false
    }
  }

  // --- inbound ---
  async function openInbound(record) {
    inboundPO.value = record
    inboundForm.warehouseId = null
    inboundForm.materialId = prField(record, 'materialId') || null
    inboundForm.materialLabel = prField(record, 'materialName') || prField(record, 'name') || '-'
    inboundForm.materialSpec = prField(record, 'materialSpec') || prField(record, 'specification') || ''
    inboundForm.quantity = 1
    inboundError.value = null
    try {
      const { data } = await client.get('/warehouses')
      warehouses.value = data || []
    } catch { warehouses.value = [] }
    inboundVisible.value = true
  }

  async function handleInbound() {
    const f = inboundForm
    if (!f.warehouseId) { inboundError.value = '请选择仓库'; return }
    if (!f.materialId) { inboundError.value = '采购申请未关联物料'; return }
    if (!f.quantity || f.quantity <= 0) { inboundError.value = '请输入有效数量'; return }
    inboundSubmitting.value = true
    try {
      await client.post('/stock-records', {
        type: 1,
        warehouseId: f.warehouseId,
        materialId: f.materialId,
        quantity: f.quantity,
        purchaseRequestId: inboundPO.value.purchaseRequestId,
      })
      inboundVisible.value = false
      await loadData()
      message.success('入库完成')
    } catch (e) {
      inboundError.value = e.message
    } finally {
      inboundSubmitting.value = false
    }
  }

  return {
    store,
    auth,
    pagination,
    columns,
    // action visibility
    showEditAction,
    showInboundAction,
    showStatusColumn,
    // edit
    editVisible, editingPO, editForm, editError, editSubmitting,
    openEdit, handleEdit,
    // inbound
    inboundVisible, inboundPO, warehouses, inboundForm, inboundError, inboundSubmitting,
    openInbound, handleInbound,
    // helpers
    prField, formatDate, formatMoney,
    // data
    loadData, handleTableChange,
  }
}
