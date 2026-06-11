package cn.tx.aiagent.task.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkflowInstance {

    private Long id;
    private String businessType;
    private Long businessId;
    private String processInstanceId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private WorkflowInstance() {}

    public static WorkflowInstance create(String businessType, Long businessId,
                                           String processInstanceId) {
        WorkflowInstance wi = new WorkflowInstance();
        wi.businessType = businessType;
        wi.businessId = businessId;
        wi.processInstanceId = processInstanceId;
        wi.status = 0;
        return wi;
    }

    public static WorkflowInstance fromPersistence(Long id, String businessType,
                                                    Long businessId,
                                                    String processInstanceId,
                                                    Integer status,
                                                    LocalDateTime createdAt,
                                                    LocalDateTime updatedAt) {
        WorkflowInstance wi = new WorkflowInstance();
        wi.id = id;
        wi.businessType = businessType;
        wi.businessId = businessId;
        wi.processInstanceId = processInstanceId;
        wi.status = status;
        wi.createdAt = createdAt;
        wi.updatedAt = updatedAt;
        return wi;
    }

    public void complete() {
        this.status = 1;
    }

    public void terminate() {
        this.status = 2;
    }
}
