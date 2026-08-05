<script setup>
import { ref, nextTick, watch } from 'vue'
import { useAgentStore } from '@/stores/agent'
import { RobotOutlined, SendOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { marked } from 'marked'

// 配置 marked 以支持换行渲染为 <br>
marked.setOptions({
  breaks: true,
  gfm: true,
})

const store = useAgentStore()

const inputMessage = ref('')
const messagesContainer = ref(null)
const sending = ref(false)

// 常用提示词快捷按钮
const quickPrompts = [
  { label: '创建采购申请', text: '帮我创建一个采购申请' },
  { label: '查看我的申请', text: '查看我的采购申请' },
]

// 解析 markdown 为 HTML
function parseMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

async function handleSend() {
  const msg = inputMessage.value.trim()
  if (!msg || sending.value) return

  // 先捕获消息再清空输入框，并用 nextTick 确保 DOM 更新
  inputMessage.value = ''
  await nextTick()
  sending.value = true

  await store.sendMessage(msg, {
    onDone: () => {
      sending.value = false
      scrollToBottom()
    },
    onError: () => {
      sending.value = false
      scrollToBottom()
    },
  })

  scrollToBottom()
}

async function handleQuickPrompt(text) {
  inputMessage.value = text
  await handleSend()
}

async function handleConfirm() {
  if (sending.value) return
  sending.value = true
  await store.sendAction('confirm')
  sending.value = false
  scrollToBottom()
}

async function handleCancel() {
  if (sending.value) return
  sending.value = true
  await store.sendAction('cancel')
  sending.value = false
  scrollToBottom()
}

async function handleModify() {
  const mod = prompt('请输入修改内容：')
  if (!mod) return
  sending.value = true
  await store.sendAction('modify ' + mod)
  sending.value = false
  scrollToBottom()
}

function handleReset() {
  store.resetSession()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 监听消息变化自动滚动
watch(() => store.messages.length, () => {
  scrollToBottom()
})

// 判断消息是否包含草稿卡片
function hasDraft(msg) {
  return msg.role === 'assistant' && msg.draft
}
</script>

<template>
  <div class="agent-chat-panel">
    <div class="chat-header">
      <div class="chat-header-left">
        <RobotOutlined class="chat-icon" />
        <div>
          <div class="chat-title">采购助手</div>
          <div class="chat-subtitle">AI 智能采购顾问</div>
        </div>
      </div>
      <div class="chat-header-actions">
        <a-button size="small" type="text" @click="handleReset" :disabled="store.messages.length === 0">
          <ReloadOutlined /> 新对话
        </a-button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <!-- 空状态 -->
      <div v-if="store.messages.length === 0" class="chat-empty">
        <div class="empty-icon">
          <RobotOutlined />
        </div>
        <div class="empty-title">你好！我是采购助手</div>
        <div class="empty-desc">告诉我你需要采购什么，我会帮你创建采购申请。例如：</div>
        <div class="empty-example">
          "帮我给朝阳项目采购50吨海螺牌PO42.5水泥，下周五前要到场"
        </div>
        <div class="quick-prompts">
          <a-button
            v-for="p in quickPrompts"
            :key="p.label"
            size="small"
            @click="handleQuickPrompt(p.text)"
          >{{ p.label }}</a-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div
        v-for="(msg, idx) in store.messages"
        :key="idx"
        :class="['chat-message', msg.role === 'user' ? 'msg-user' : 'msg-assistant']"
      >
        <div class="msg-avatar">
          <RobotOutlined v-if="msg.role === 'assistant'" />
          <span v-else>👤</span>
        </div>
        <div class="msg-body">
          <!-- 文本内容 -->
          <div class="msg-content" v-if="msg.content">
            <template v-if="msg.role === 'assistant'">
              <!-- eslint-disable-next-line vue/no-v-html -->
              <span class="markdown-body" v-html="parseMarkdown(msg.content)"></span>
              <span v-if="sending && idx === store.messages.length - 1" class="cursor-blink">▌</span>
            </template>
            <template v-else>{{ msg.content }}</template>
          </div>

          <!-- 加载指示 -->
          <div v-if="sending && idx === store.messages.length - 1 && !msg.content" class="msg-loading">
            <span class="dot-pulse"></span>
          </div>

          <!-- 草稿确认卡片 -->
          <div v-if="hasDraft(msg)" class="draft-card">
            <div class="draft-card-title">📋 采购申请草稿</div>
            <div class="draft-card-body">
              <div class="draft-row">
                <span class="draft-label">编号</span>
                <span class="draft-value">{{ msg.draft.prCode }}</span>
              </div>
              <div class="draft-row">
                <span class="draft-label">项目</span>
                <span class="draft-value">{{ msg.draft.projectName || '-' }}</span>
              </div>
              <div class="draft-row">
                <span class="draft-label">物料</span>
                <span class="draft-value">{{ msg.draft.materialName }} {{ msg.draft.materialSpec || '' }}</span>
              </div>
              <div class="draft-row" v-if="msg.draft.brand">
                <span class="draft-label">品牌</span>
                <span class="draft-value">{{ msg.draft.brand }}</span>
              </div>
              <div class="draft-row">
                <span class="draft-label">数量</span>
                <span class="draft-value">{{ msg.draft.quantity }} {{ msg.draft.unit }}</span>
              </div>
              <div class="draft-row">
                <span class="draft-label">到场日期</span>
                <span class="draft-value">{{ msg.draft.arrivalDate }}</span>
              </div>
            </div>
            <div class="draft-card-actions">
              <a-button type="primary" size="small" @click="handleConfirm" :loading="sending">
                ✅ 确认提交
              </a-button>
              <a-button size="small" @click="handleModify" :disabled="sending">
                ✏️ 修改
              </a-button>
              <a-button size="small" danger @click="handleCancel" :disabled="sending">
                ❌ 取消
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <a-textarea
        v-model:value="inputMessage"
        placeholder="输入采购需求，例如：帮我给XX项目采购50吨水泥..."
        :auto-size="{ minRows: 1, maxRows: 4 }"
        @pressEnter="(e) => { if (!e.shiftKey) { e.preventDefault(); handleSend(); } }"
        :disabled="sending"
      />
      <a-button
        type="primary"
        shape="circle"
        size="large"
        @click="handleSend"
        :loading="sending"
        :disabled="!inputMessage.trim()"
      >
        <template #icon><SendOutlined /></template>
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.agent-chat-panel {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-icon {
  font-size: 24px;
  color: #6366f1;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chat-subtitle {
  font-size: 12px;
  color: #9ca3af;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  text-align: center;
  padding: 40px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #6366f1;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  margin-bottom: 16px;
}

.empty-example {
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
  color: #6366f1;
  margin-bottom: 20px;
  max-width: 400px;
}

.quick-prompts {
  display: flex;
  gap: 8px;
}

.chat-message {
  display: flex;
  gap: 10px;
  max-width: 85%;
}

.msg-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-assistant {
  align-self: flex-start;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  font-size: 14px;
  flex-shrink: 0;
}

.msg-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-content {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-user .msg-content {
  background: #6366f1;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-assistant .msg-content {
  background: #f3f4f6;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

/* markdown 渲染样式 */
.msg-assistant .msg-content :deep(.markdown-body) {
  line-height: 1.7;
}

.msg-assistant .msg-content :deep(.markdown-body p) {
  margin: 0 0 0.5em;
}

.msg-assistant .msg-content :deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}

.msg-assistant .msg-content :deep(.markdown-body strong) {
  font-weight: 600;
  color: #1f2937;
}

.msg-assistant .msg-content :deep(.markdown-body em) {
  font-style: italic;
}

.msg-assistant .msg-content :deep(.markdown-body code) {
  background: rgba(0,0,0,0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

.msg-assistant .msg-content :deep(.markdown-body pre) {
  background: #1f2937;
  color: #e5e7eb;
  padding: 12px 14px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  font-size: 0.85em;
}

.msg-assistant .msg-content :deep(.markdown-body pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
  font-size: inherit;
}

.msg-assistant .msg-content :deep(.markdown-body ul),
.msg-assistant .msg-content :deep(.markdown-body ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.msg-assistant .msg-content :deep(.markdown-body li) {
  margin-bottom: 2px;
}

.msg-assistant .msg-content :deep(.markdown-body h1),
.msg-assistant .msg-content :deep(.markdown-body h2),
.msg-assistant .msg-content :deep(.markdown-body h3),
.msg-assistant .msg-content :deep(.markdown-body h4) {
  margin: 8px 0 4px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.msg-assistant .msg-content :deep(.markdown-body h1) { font-size: 1.3em; }
.msg-assistant .msg-content :deep(.markdown-body h2) { font-size: 1.15em; }
.msg-assistant .msg-content :deep(.markdown-body h3) { font-size: 1.05em; }
.msg-assistant .msg-content :deep(.markdown-body h4) { font-size: 1em; }

.msg-assistant .msg-content :deep(.markdown-body blockquote) {
  border-left: 3px solid #6366f1;
  padding: 4px 12px;
  margin: 6px 0;
  color: #6b7280;
  background: rgba(99,102,241,0.04);
  border-radius: 0 4px 4px 0;
}

.msg-assistant .msg-content :deep(.markdown-body hr) {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 10px 0;
}

.msg-assistant .msg-content :deep(.markdown-body a) {
  color: #6366f1;
  text-decoration: underline;
}

.msg-assistant .msg-content :deep(.markdown-body table) {
  border-collapse: collapse;
  margin: 8px 0;
  font-size: 0.9em;
}

.msg-assistant .msg-content :deep(.markdown-body th),
.msg-assistant .msg-content :deep(.markdown-body td) {
  border: 1px solid #e5e7eb;
  padding: 6px 10px;
  text-align: left;
}

.msg-assistant .msg-content :deep(.markdown-body th) {
  background: #f9fafb;
  font-weight: 600;
}

.msg-loading {
  padding: 10px 14px;
}

.dot-pulse::after {
  content: '思考中...';
  animation: dotPulse 1.5s infinite;
  color: #9ca3af;
  font-size: 13px;
}

@keyframes dotPulse {
  0%, 100% { content: '思考中...'; }
  33% { content: '思考中.'; }
  66% { content: '思考中..'; }
}

.cursor-blink {
  animation: blink 1s infinite;
  color: #6366f1;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 草稿卡片 */
.draft-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  width: 320px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.draft-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.draft-card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.draft-row {
  display: flex;
  gap: 8px;
  font-size: 13px;
}

.draft-label {
  color: #9ca3af;
  min-width: 56px;
}

.draft-value {
  color: #374151;
  font-weight: 500;
}

.draft-card-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid #f0f0f0;
}

.chat-input-area :deep(.ant-input) {
  border-radius: 8px;
}
</style>
