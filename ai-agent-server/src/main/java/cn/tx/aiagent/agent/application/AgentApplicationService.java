package cn.tx.aiagent.agent.application;

import cn.tx.aiagent.agent.Constants;
import cn.tx.aiagent.agent.domain.AgentSession;
import cn.tx.aiagent.agent.domain.ChatEvent;
import cn.tx.aiagent.agent.infrastructure.AgentSessionStore;
import cn.tx.aiagent.common.config.LoginContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Agent 对话编排服务。
 * 管理会话状态机、协调 LLM 调用和工具执行、生成 SSE 事件流。
 */
@Service
public class AgentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AgentApplicationService.class);

    private final ChatClient chatClient;
    private final AgentSessionStore sessionStore;

    public AgentApplicationService(ChatClient chatClient,
                                    AgentSessionStore sessionStore) {
        this.chatClient = chatClient;
        this.sessionStore = sessionStore;
    }

    /**
     * 处理用户消息，返回 SSE 事件流。
     *
     * @param sessionId 会话 ID，null 则创建新会话
     * @param message   用户消息
     * @return SSE 事件流
     */
    public Flux<ChatEvent> chat(String sessionId, String message) {
        AgentSession session = loadOrCreateSession(sessionId);

        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            return Flux.just(new ChatEvent.Error("用户未登录"));
        }

        // 记录用户消息
        session.addMessage(new UserMessage(message));

        try {
            // 发送会话初始化事件（含 sessionId）
            Flux<ChatEvent> initEvent = Flux.just(
                    new ChatEvent.SessionInit(session.getSessionId()));

            // LLM 调用（使用 call() 非流式，确保 ToolCallingAdvisor 被正确触发）
            // DeepSeek 适配器在流式路径中 Advisor 链未被调用，导致工具不执行
            // call() 走同步路径，Spring AI 会正确执行工具后再返回最终文本
            String content = chatClient.prompt()
                    .messages(session.getMessages())
                    .toolContext(Map.of(Constants.USER_ID, userId, Constants.SESSION_ID, session.getSessionId()))
                    .call()
                    .content();
            session.addMessage(new AssistantMessage(content));
            return initEvent.concatWith(Flux.just(new ChatEvent.Thinking(content)));

        } catch (Exception e) {
            log.error("Agent 对话异常", e);
            return Flux.just(new ChatEvent.Error("启动对话失败，请重试"));
        }
    }

    /**
     * 加载或创建会话。
     */
    private AgentSession loadOrCreateSession(String sessionId) {
        sessionStore.cleanExpired();

        if (sessionId != null && !sessionId.isBlank()) {
            AgentSession existing = sessionStore.findById(sessionId);
            if (existing != null) {
                if (existing.isExpired(30)) {
                    sessionStore.remove(sessionId);
                } else {
                    return existing;
                }
            }
        }
        return sessionStore.create();
    }
}
