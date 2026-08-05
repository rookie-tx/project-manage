package cn.tx.aiagent.agent.api;

/**
 * Agent 聊天请求。
 *
 * @param sessionId 会话 ID，首次对话可不传，后续对话必须传回服务端返回的 sessionId
 * @param message   用户消息内容
 */
public record ChatRequest(String sessionId, String message) {}
