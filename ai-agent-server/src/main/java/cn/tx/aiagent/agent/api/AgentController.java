package cn.tx.aiagent.agent.api;

import cn.tx.aiagent.agent.application.AgentApplicationService;
import cn.tx.aiagent.agent.domain.ChatEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Agent 对话控制器，提供 SSE 流式对话接口。
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentApplicationService agentService;

    public AgentController(AgentApplicationService agentService) {
        this.agentService = agentService;
    }

    /**
     * 发起对话，返回 SSE 事件流。
     * <p>
     * SSE 事件格式：data: {"type":"...", "data":...}\n\n
     * 前端使用 fetch + ReadableStream 消费，逐行解析 JSON。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String sessionId = request.sessionId();
        String message = request.message();

        if (message == null || message.isBlank()) {
            return Flux.just(toSse("error", quote("消息不能为空")));
        }

        return agentService.chat(sessionId, message)
                .map(event -> switch (event) {
                    case ChatEvent.SessionInit si -> toSse("session_init", quote(si.sessionId()));
                    case ChatEvent.Thinking t -> toSse("thinking", quote(t.delta()));
                    case ChatEvent.ToolCall tc -> toSse("tool_call",
                            "{\"tool\":\"" + esc(tc.toolName()) + "\",\"args\":\"" + esc(tc.args()) + "\"}");
                    case ChatEvent.ToolResult tr -> toSse("tool_result",
                            "{\"tool\":\"" + esc(tr.toolName()) + "\",\"result\":\"" + esc(tr.result()) + "\"}");
                    case ChatEvent.Done d -> toSse("done", quote(d.message()));
                    case ChatEvent.Error e -> toSse("error", quote(e.message()));
                });
    }

    /**
     * 构建 SSE 事件 JSON 负载。
     * 不手动添加 "data:" 前缀和 "\n\n"——Spring WebFlux 对 text/event-stream
     * 响应会自动添加 SSE 帧格式，手动添加会导致 "data:data:" 重复前缀。
     */
    private String toSse(String type, String dataJson) {
        return "{\"type\":\"" + type + "\",\"data\":" + dataJson + "}";
    }

    /** 将字符串转为 JSON 字符串值（加引号并转义） */
    private String quote(String s) {
        if (s == null) return "\"\"";
        return "\"" + esc(s) + "\"";
    }

    /** 转义 JSON 字符串中的特殊字符 */
    private String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
