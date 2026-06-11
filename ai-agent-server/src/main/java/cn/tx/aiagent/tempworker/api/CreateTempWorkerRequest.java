package cn.tx.aiagent.tempworker.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateTempWorkerRequest {
    @NotNull private Long projectId;
    @NotBlank private String name;
    @NotBlank private String phone;
    private String workerType;
    @NotNull private LocalDate entryDate;
}
