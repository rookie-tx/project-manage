<script setup>
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Spin, message, Button } from 'ant-design-vue'
import ProjectForm from '@/components/project/ProjectForm.vue'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const route = useRoute()
const projectStore = useProjectStore()

onMounted(async () => {
  try {
    await projectStore.fetchDetail(route.params.id)
  } catch {
    // error is in store
  }
})

const initialValues = computed(() => {
  const p = projectStore.current
  if (!p) return {}
  return {
    name: p.name,
    type: p.type,
    location: p.location,
    clientName: p.clientName,
    supervisorUnit: p.supervisorUnit,
    projectManagerId: p.projectManagerId,
    plannedStartDate: p.plannedStartDate,
    plannedEndDate: p.plannedEndDate,
    actualStartDate: p.actualStartDate,
    actualEndDate: p.actualEndDate,
    status: p.status,
    remark: p.remark,
  }
})

async function handleSubmit(formData) {
  try {
    await projectStore.update(route.params.id, formData)
    message.success('项目更新成功')
    router.back()
  } catch {
    message.error(projectStore.error || '更新失败')
  }
}

function handleCancel() {
  router.back()
}
</script>

<template>
  <div class="project-edit-page">
    <Spin :spinning="projectStore.loading && !projectStore.current">
      <template v-if="projectStore.error">
        <p class="edit-error">{{ projectStore.error }}</p>
        <Button @click="router.push('/projects')">返回列表</Button>
      </template>

      <template v-else-if="projectStore.current">
        <h2>编辑项目 - {{ projectStore.current.name }}</h2>
        <ProjectForm
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
.project-edit-page {
  padding: 24px;
}

.project-edit-page h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.edit-error {
  color: #ff4d4f;
  font-size: 16px;
  margin-bottom: 16px;
}
</style>
