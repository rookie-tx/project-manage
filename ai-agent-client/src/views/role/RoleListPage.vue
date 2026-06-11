<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoleManageStore } from '@/stores/roleManage'
import { useAuthStore } from '@/stores/auth'
import { Table, Button, Tag, Alert, Modal, Form, FormItem, Input, Checkbox, Space, Divider } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'

const store = useRoleManageStore()
const auth = useAuthStore()

const modalVisible = ref(false)
const modalTitle = ref('创建角色')
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const formState = reactive({
  code: '',
  name: '',
  description: '',
  permissionIds: [],
})
const error = ref(null)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  { title: '系统保护', dataIndex: 'isProtected', key: 'isProtected', width: 100 },
  { title: '操作', key: 'action', width: 200 },
]

// 按资源前缀分组权限
const permissionGroups = computed(() => {
  const groups = {}
  for (const p of store.permissions) {
    const prefix = p.code.split(':')[0]
    if (!groups[prefix]) groups[prefix] = { label: '', permissions: [] }
    groups[prefix].label = prefixMap[prefix] || prefix
    groups[prefix].permissions.push(p)
  }
  return groups
})

const prefixMap = {
  user: '用户管理',
  role: '角色管理',
  project: '项目管理',
  pr: '采购申请',
  task: '审批任务',
}

const rules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

onMounted(() => {
  store.fetchRoles()
  store.fetchPermissions()
})

function handleCreate() {
  editingId.value = null
  modalTitle.value = '创建角色'
  formState.code = ''
  formState.name = ''
  formState.description = ''
  formState.permissionIds = []
  error.value = null
  modalVisible.value = true
}

async function handleEdit(record) {
  editingId.value = record.id
  modalTitle.value = '编辑角色'
  formState.code = record.code
  formState.name = record.name
  formState.description = record.description || ''
  error.value = null
  try {
    const permIds = await store.fetchRolePermissions(record.id)
    formState.permissionIds = permIds || []
  } catch {
    formState.permissionIds = []
  }
  modalVisible.value = true
}

async function handleSubmit() {
  error.value = null
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      code: formState.code,
      name: formState.name,
      description: formState.description || '',
      permissionIds: formState.permissionIds,
    }
    if (editingId.value) {
      await store.updateRole(editingId.value, payload)
    } else {
      await store.createRole(payload)
    }
    modalVisible.value = false
    store.fetchRoles()
  } catch (e) {
    if (e.errorFields) return
    error.value = e.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

function handleDelete(record) {
  if (record.isProtected) {
    Modal.warning({ title: '提示', content: '系统内置角色不可删除' })
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: `确认删除角色「${record.name}」？`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      await store.deleteRole(record.id)
      store.fetchRoles()
    },
  })
}

function isProtected(record) {
  return record.isProtected === 1 || record.protected === true
}
</script>

<template>
  <div class="role-list-page">
    <div class="page-header">
      <h2>角色管理</h2>
      <Button v-if="auth.hasPermission('role:create')" type="primary" @click="handleCreate">
        <PlusOutlined />
        创建角色
      </Button>
    </div>

    <Alert v-if="store.error" type="error" :message="store.error" closable style="margin-bottom: 16px" />

    <Table
      :columns="columns"
      :data-source="store.roles"
      :loading="store.loading"
      row-key="id"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'isProtected'">
          <Tag v-if="isProtected(record)" color="orange">是</Tag>
          <span v-else>否</span>
        </template>
        <template v-if="column.key === 'action'">
          <Space>
            <a
                v-if="auth.hasPermission('role:update')"
                @click="handleEdit(record)">编辑</a>
            <a
              v-if="auth.hasPermission('role:delete')"
              :style="isProtected(record) ? { color: '#ccc', cursor: 'not-allowed' } : { color: '#ff4d4f' }"
              @click="isProtected(record) ? null : handleDelete(record)"
            >删除</a>
          </Space>
        </template>
      </template>
    </Table>

    <Modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="submitting"
      @ok="handleSubmit"
      width="640px"
    >
      <Alert v-if="error" type="error" :message="error" closable style="margin-bottom: 16px" />

      <Form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <FormItem v-if="!editingId" label="编码" name="code">
          <Input v-model:value="formState.code" placeholder="英文编码，如 auditor" :maxlength="50" :disabled="!!editingId" />
        </FormItem>
        <FormItem label="名称" name="name">
          <Input v-model:value="formState.name" placeholder="角色名称" :maxlength="50" />
        </FormItem>
        <FormItem label="描述">
          <Input v-model:value="formState.description" placeholder="可选描述" :maxlength="200" />
        </FormItem>

        <Divider />
        <div class="permission-section">
          <h4>权限分配</h4>
          <Checkbox.Group v-model:value="formState.permissionIds" style="width: 100%">
            <template v-for="group in permissionGroups" :key="group.label">
              <div class="permission-group">
                <div class="group-label">{{ group.label }}</div>
                <Checkbox
                  v-for="perm in group.permissions"
                  :key="perm.id"
                  :value="perm.id"
                >
                  {{ perm.name }} <span style="color: #999; font-size: 12px">({{ perm.code }})</span>
                </Checkbox>
              </div>
            </template>
          </Checkbox.Group>
        </div>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>
.role-list-page {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
}

.permission-section {
  max-height: 300px;
  overflow-y: auto;
}

.permission-section h4 {
  margin: 0 0 12px;
  font-size: 14px;
}

.permission-group {
  margin-bottom: 16px;
}

.group-label {
  font-weight: 600;
  font-size: 13px;
  color: #1a1a1a;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}
</style>
