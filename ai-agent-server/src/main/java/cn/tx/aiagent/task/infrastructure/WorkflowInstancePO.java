package cn.tx.aiagent.task.infrastructure;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class WorkflowInstancePO {

    private Long id;
    private String businessType;
    private Long businessId;
    private String processInstanceId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
