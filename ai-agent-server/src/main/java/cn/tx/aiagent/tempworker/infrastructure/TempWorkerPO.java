package cn.tx.aiagent.tempworker.infrastructure;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TempWorkerPO {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String phone;
    private Long projectId;
    private String workerType;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
