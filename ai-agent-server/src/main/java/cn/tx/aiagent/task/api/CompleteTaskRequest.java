package cn.tx.aiagent.task.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteTaskRequest {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    private String comment;
}
