package cn.tx.aiagent.warehouse.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateStockRecordRequest {
    @NotNull
    private Long warehouseId;
    @NotNull
    private Long materialId;
    @NotNull
    private Integer type;
    @NotNull @Positive
    private Double quantity;
    private Long purchaseRequestId;
    private Long projectId;
}
