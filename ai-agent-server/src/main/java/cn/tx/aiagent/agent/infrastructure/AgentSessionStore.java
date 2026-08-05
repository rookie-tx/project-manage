package cn.tx.aiagent.agent.infrastructure;

import cn.tx.aiagent.agent.domain.AgentSession;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 会话存储（内存实现）。
 * 后续可替换为 Redis 实现。
 */
@Component
public class AgentSessionStore {

    private final ConcurrentHashMap<String, AgentSession> sessions = new ConcurrentHashMap<>();

    private static final int DEFAULT_TTL_MINUTES = 30;

    /** 创建新会话 */
    public AgentSession create() {
        AgentSession session = AgentSession.create();
        sessions.put(session.getSessionId(), session);
        return session;
    }

    /** 按 ID 查找会话 */
    public AgentSession findById(String sessionId) {
        return sessions.get(sessionId);
    }

    /** 移除会话 */
    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    /** 清理过期会话 */
    public void cleanExpired() {
        sessions.values().removeIf(s -> s.isExpired(DEFAULT_TTL_MINUTES));
    }
}
