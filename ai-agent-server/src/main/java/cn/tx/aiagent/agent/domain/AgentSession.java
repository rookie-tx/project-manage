package cn.tx.aiagent.agent.domain;

import lombok.Getter;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agent 对话会话，管理多轮对话的上下文和状态。
 */
@Getter
public class AgentSession {

    private final String sessionId;
    private final List<Message> messages;
    private SessionState state;
    private Long draftPrId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    private AgentSession(String sessionId) {
        this.sessionId = sessionId;
        this.messages = new ArrayList<>();
        this.state = SessionState.IDLE;
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 创建新会话
     */
    public static AgentSession create() {
        return new AgentSession(UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 添加消息并更新活跃时间
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 状态转换：进入澄清状态
     */
    public void startClarifying() {
        this.state = SessionState.CLARIFYING;
    }

    /**
     * 状态转换：等待用户确认
     */
    public void awaitConfirmation(Long prId) {
        this.state = SessionState.AWAITING_CONFIRMATION;
        this.draftPrId = prId;
    }

    /**
     * 状态转换：对话完成
     */
    public void complete() {
        this.state = SessionState.COMPLETED;
    }

    /**
     * 重置草稿（用户取消或修改草稿时）
     */
    public void clearDraft() {
        this.draftPrId = null;
        this.state = SessionState.CLARIFYING;
    }

    /**
     * 判断会话是否过期（超过指定分钟数）
     */
    public boolean isExpired(int ttlMinutes) {
        return lastActiveAt.plusMinutes(ttlMinutes).isBefore(LocalDateTime.now());
    }
}
