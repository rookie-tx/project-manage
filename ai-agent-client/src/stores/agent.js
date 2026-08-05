import { defineStore } from 'pinia'
import { ref } from 'vue'

const API_BASE = 'http://localhost:8088/api'
const REQUEST_TIMEOUT_MS = 120_000 // 2 分钟超时
const TYPEWRITER_SPEED = 25 // 毫秒/字符

export const useAgentStore = defineStore('agent', () => {
  const sessionId = ref(null)
  const messages = ref([])
  const loading = ref(false)

  // ---------- 打字机效果 ----------

  let typewriterTimer = null
  let typewriterTarget = null // 当前正在打字的 assistantMsg 引用

  /** 启动打字机，将 _buffer 中的内容逐字移到 content */
  function ensureTypewriter(msg) {
    // 如果目标变了，先停掉旧的
    if (typewriterTarget && typewriterTarget !== msg) {
      stopTypewriter()
    }
    typewriterTarget = msg

    if (typewriterTimer) return // 已在运行

    typewriterTimer = setInterval(() => {
      if (!msg._buffer || msg._buffer.length === 0) {
        // 缓冲区暂时空了（等 SSE 推送更多内容），暂停但不停止
        return
      }
      // 一次取 1~3 个字符，模拟真实打字节奏
      const take = Math.min(3, msg._buffer.length)
      msg.content += msg._buffer.substring(0, take)
      msg._buffer = msg._buffer.substring(take)
    }, TYPEWRITER_SPEED)
  }

  function stopTypewriter() {
    if (typewriterTimer) {
      clearInterval(typewriterTimer)
      typewriterTimer = null
    }
    typewriterTarget = null
  }

  /** 立即输出缓冲区剩余内容 */
  function flushTypewriter(msg) {
    stopTypewriter()
    if (msg._buffer) {
      msg.content += msg._buffer
      msg._buffer = ''
    }
  }

  // ---------- SSE 通信 ----------

  /**
   * 发送消息到 Agent，使用 fetch + ReadableStream 消费 SSE 流。
   *
   * @param {string} message 用户消息文本
   * @param {object} callbacks 回调：onThinking, onDraftCard, onDone, onError
   */
  async function sendMessage(message, callbacks = {}) {
    loading.value = true
    stopTypewriter() // 停止上一轮的打字机

    messages.value.push({ role: 'user', content: message, draft: null })

    const assistantMsg = { role: 'assistant', content: '', draft: null, _buffer: '' }
    messages.value.push(assistantMsg)

    let timeoutId = null

    try {
      const controller = new AbortController()
      timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS)

      const response = await fetch(API_BASE + '/agent/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        signal: controller.signal,
        body: JSON.stringify({
          sessionId: sessionId.value || null,
          message: message,
        }),
      })

      clearTimeout(timeoutId)
      timeoutId = null

      if (!response.ok) {
        throw new Error('请求失败 (HTTP ' + response.status + ')')
      }

      const contentType = response.headers.get('content-type') || ''
      if (!contentType.includes('text/event-stream')) {
        const text = await response.text()
        let errMsg = '服务器返回异常: ' + contentType
        try {
          const json = JSON.parse(text)
          if (json.msg) errMsg = json.msg
        } catch (_) {
          if (text.length < 200) errMsg = text
        }
        throw new Error(errMsg)
      }

      if (!response.body) {
        throw new Error('响应体为空，可能是认证或网络问题')
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed || trimmed === 'data:') continue

          if (trimmed.startsWith('data:')) {
            const jsonStr = trimmed.substring(5).trim()
            if (!jsonStr) continue
            try {
              const event = JSON.parse(jsonStr)
              handleEvent(event, assistantMsg, callbacks)
            } catch (e) {
              console.warn('[Agent] SSE 解析失败:', jsonStr.substring(0, 80), e)
            }
          }
        }
      }

      // 流正常结束 — 确保打字机输出完毕
      flushTypewriter(assistantMsg)
      if (assistantMsg.content === '' && !assistantMsg.draft) {
        assistantMsg.content = '（服务端未返回内容）'
      }
      if (callbacks.onDone) callbacks.onDone('stream_ended')

    } catch (e) {
      clearTimeout(timeoutId)
      flushTypewriter(assistantMsg)
      if (e.name === 'AbortError') {
        assistantMsg.content = '请求超时，请检查网络后重试'
      } else {
        assistantMsg.content = '请求失败: ' + e.message
      }
      if (callbacks.onError) callbacks.onError(e.message)
    } finally {
      clearTimeout(timeoutId)
      loading.value = false
    }
  }

  function handleEvent(event, assistantMsg, callbacks) {
    switch (event.type) {
      case 'session_init': {
        const sid = event.data
        if (sid && typeof sid === 'string') {
          sessionId.value = sid
        }
        break
      }
      case 'thinking': {
        const data = event.data
        if (typeof data === 'string') {
          assistantMsg._buffer += data
          ensureTypewriter(assistantMsg)
        }
        if (callbacks.onThinking) callbacks.onThinking(data)
        break
      }
      case 'tool_call': {
        const toolName = event.data?.tool || event.data
        if (typeof toolName === 'string') {
          assistantMsg._buffer += '\n🔍 调用工具：' + toolName + '\n'
          ensureTypewriter(assistantMsg)
        }
        break
      }
      case 'tool_result': {
        break
      }
      case 'draft_card': {
        // 草稿卡片出现时先 flush 打字机，确保卡片之前的文字全部输出
        flushTypewriter(assistantMsg)
        assistantMsg.draft = event.data
        if (callbacks.onDraftCard) callbacks.onDraftCard(event.data)
        break
      }
      case 'done': {
        flushTypewriter(assistantMsg)
        const data = event.data
        if (data && typeof data === 'string' && data !== 'completed' && data !== 'draft_pending') {
          assistantMsg.content += data
        }
        if (callbacks.onDone) callbacks.onDone(event.data)
        break
      }
      case 'error': {
        flushTypewriter(assistantMsg)
        assistantMsg.content = event.data
        if (callbacks.onError) callbacks.onError(event.data)
        break
      }
    }
  }

  /** 发送按钮操作（确认/修改/取消） */
  async function sendAction(action) {
    return sendMessage('__ACTION__:' + action)
  }

  /** 重置会话 */
  function resetSession() {
    stopTypewriter()
    sessionId.value = null
    messages.value = []
    loading.value = false
  }

  return {
    sessionId,
    messages,
    loading,
    sendMessage,
    sendAction,
    resetSession,
  }
})
