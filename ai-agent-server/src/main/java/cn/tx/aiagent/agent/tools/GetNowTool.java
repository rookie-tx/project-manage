package cn.tx.aiagent.agent.tools;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GetNowTool extends AbstractTool<GetNowTool.GetNowRequest> {

    @Override
    String getId() {
        return "get_now";
    }

    @Override
    String getDescription() {
        return "获取当前时间";
    }

    @Override
    Object doCall(String toolInput, @Nullable ToolContext toolContext) {
        return LocalDateTime.now();
    }

    @Override
    Class<GetNowRequest> getArgumentType() {
        return GetNowRequest.class;
    }

    public record GetNowRequest() {}

}
