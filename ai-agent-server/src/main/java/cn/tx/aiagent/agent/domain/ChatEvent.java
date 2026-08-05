package cn.tx.aiagent.agent.domain;

/**
 * SSE 事件类型，前端根据不同类型渲染不同 UI 组件。
 */
public sealed interface ChatEvent {

    /** 会话初始化，携带 sessionId，前端应保存此 ID 用于后续请求 */
    record SessionInit(String sessionId) implements ChatEvent {}

    /** LLM 正在思考，增量文本输出 */
    record Thinking(String delta) implements ChatEvent {}

    /** LLM 调用工具通知 */
    record ToolCall(String toolName, String args) implements ChatEvent {}

    /** 工具执行结果通知 */
    record ToolResult(String toolName, String result) implements ChatEvent {}

    /** 本轮对话处理完毕 */
    record Done(String message) implements ChatEvent {}

    /** 处理异常 */
    record Error(String message) implements ChatEvent {}
}
