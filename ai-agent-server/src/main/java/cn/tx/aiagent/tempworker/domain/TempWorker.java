package cn.tx.aiagent.tempworker.domain;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class TempWorker {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String phone;
    private Long projectId;
    private String workerType;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private WorkerStatus status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private TempWorker() {}

    public static TempWorker create(Long userId, Long projectId, String workerType, LocalDate entryDate) {
        TempWorker w = new TempWorker();
        w.userId = userId; w.projectId = projectId; w.workerType = workerType;
        w.entryDate = entryDate; w.status = WorkerStatus.ACTIVE; w.isDeleted = 0;
        return w;
    }

    public static TempWorker fromPersistence(Long id, Long userId, String username, String name, String phone, Long projectId, String workerType,
                                              LocalDate entryDate, LocalDate exitDate, int statusCode,
                                              int isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        TempWorker w = new TempWorker();
        w.id = id; w.userId = userId; w.username = username; w.name = name; w.phone = phone; w.projectId = projectId; w.workerType = workerType;
        w.entryDate = entryDate; w.exitDate = exitDate; w.status = WorkerStatus.fromCode(statusCode);
        w.isDeleted = isDeleted; w.createdAt = createdAt; w.updatedAt = updatedAt;
        return w;
    }

    public void markLeft(LocalDate exitDate) { this.exitDate = exitDate; this.status = WorkerStatus.LEFT; }
    public boolean isActive() { return status == WorkerStatus.ACTIVE; }
    public void markDeleted() { this.isDeleted = 1; }
    public boolean isDeleted() { return isDeleted != null && isDeleted == 1; }
}
