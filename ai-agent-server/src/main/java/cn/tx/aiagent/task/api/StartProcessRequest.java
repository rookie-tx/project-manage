package cn.tx.aiagent.task.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class StartProcessRequest {

    @NotBlank
    private String businessType;

    @NotNull
    private Long businessId;

    private Map<String, Object> variables;
}
