package cn.tx.aiagent.task.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private String taskId;
    private String businessType;
    private Long businessId;
    private String businessTitle;
    private String businessSubtitle;
    private String nodeName;
    private LocalDateTime createTime;
}
