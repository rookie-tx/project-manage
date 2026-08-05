package cn.tx.aiagent.agent.tools;

import cn.tx.aiagent.agent.Constants;
import cn.tx.aiagent.agent.domain.AgentSession;
import cn.tx.aiagent.agent.infrastructure.AgentSessionStore;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.augment.ToolInputSchemaAugmenter;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class AbstractTool<T extends Record> implements ToolCallback {

    private static final DefaultToolCallResultConverter CONVERTER = new DefaultToolCallResultConverter();

    static final String toolMessageFormat = """
                            {
                              "type": "tool_result",
                              "tool_name": "%s",
                              "tool_input": "%s",
                              "tool_output": "%s"
                            }
                            """;

    @Autowired
    private AgentSessionStore sessionStore;

    @Override
    @NonNull
    public String call(@NonNull String toolInput) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @NonNull
    public String call(@NonNull String toolInput, ToolContext toolContext) {
        Object response = doCall(toolInput, toolContext);
        String result = CONVERTER.convert(response, getResultType());

        saveToolCallMessage(toolContext, toolInput, result);
        return result;
    }

    @Override
    @NonNull
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name(getId())
                .inputSchema(getInputSchema())
                .description(getDescription())
                .build();
    }

    protected void saveToolCallMessage(ToolContext toolContext, String input, String response) {
        Optional.ofNullable(toolContext)
                .map(ToolContext::getContext)
                .map(m -> m.get("sessionId").toString())
                .ifPresent(sessionId -> {
                    AgentSession session = sessionStore.findById(sessionId);
                    UserMessage userMessage = new UserMessage(String.format(toolMessageFormat, getId(), input, response));
                    session.addMessage(userMessage);
                });
    }

    protected String getInputSchema() {
        String jsonSchemaString = """
                  {"type": "object"}
                """;
        List<ToolInputSchemaAugmenter.AugmentedArgumentType> argumentTypes = ToolInputSchemaAugmenter
                .toAugmentedArgumentTypes(getArgumentType());
        return ToolInputSchemaAugmenter.augmentToolInputSchema(jsonSchemaString, argumentTypes);
    }

    protected Long getUserId(ToolContext toolContext) {
        return (Long) toolContext.getContext().get(Constants.USER_ID);
    }

    /**
     * 工具名称
     */
    protected String getName() {
        return getId();
    }

    /**
     * 工具返回参数类型
     */
    protected Class<?> getResultType() {
        return String.class;
    }

    /**
     * 工具ID
     */
    abstract String getId();

    /**
     * 获取说明
     */
    abstract String getDescription();

    /**
     * 实际执行
     */
    abstract Object doCall(String toolInput, @Nullable ToolContext toolContext);

    /**
     * 获取入参类型
     */
    abstract Class<T> getArgumentType();
}
