package cn.tx.aiagent.task.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WorkflowTask {

    private final String taskId;
    private final String nodeName;
    private final String assignee;
    private final String businessType;
    private final Long businessId;
    private final String businessTitle;
    private final String businessSubtitle;
    private final LocalDateTime createTime;

    private WorkflowTask(String taskId, String nodeName, String assignee,
                         String businessType, Long businessId, String businessTitle,
                         String businessSubtitle, LocalDateTime createTime) {
        this.taskId = taskId;
        this.nodeName = nodeName;
        this.assignee = assignee;
        this.businessType = businessType;
        this.businessId = businessId;
        this.businessTitle = businessTitle;
        this.businessSubtitle = businessSubtitle;
        this.createTime = createTime;
    }

    public static WorkflowTask fromPersistence(String taskId, String nodeName,
                                                String assignee, String businessType,
                                                Long businessId, String businessTitle,
                                                String businessSubtitle,
                                                LocalDateTime createTime) {
        return new WorkflowTask(taskId, nodeName, assignee, businessType, businessId,
                businessTitle, businessSubtitle, createTime);
    }
}
