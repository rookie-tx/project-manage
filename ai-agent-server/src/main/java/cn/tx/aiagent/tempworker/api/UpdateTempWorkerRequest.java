package cn.tx.aiagent.tempworker.api;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateTempWorkerRequest {
    private String name;
    private String workerType;
    private LocalDate entryDate;
    private LocalDate exitDate;
}
