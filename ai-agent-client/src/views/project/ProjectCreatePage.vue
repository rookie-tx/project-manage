<script setup>
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import ProjectForm from '@/components/project/ProjectForm.vue'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const projectStore = useProjectStore()

async function handleSubmit(formData) {
  try {
    await projectStore.create(formData)
    message.success('项目创建成功')
    router.push('/projects')
  } catch {
    message.error(projectStore.error || '创建失败')
  }
}

function handleCancel() {
  router.push('/projects')
}
</script>

<template>
  <div class="project-create-page">
    <h2>新建项目</h2>
    <ProjectForm mode="create" @submit="handleSubmit" @cancel="handleCancel" />
  </div>
</template>

<style scoped>
.project-create-page {
  padding: 24px;
}

.project-create-page h2 {
  margin: 0 0 24px;
  font-size: 20px;
}
</style>
