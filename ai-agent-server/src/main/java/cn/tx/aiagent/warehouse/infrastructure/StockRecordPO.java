package cn.tx.aiagent.warehouse.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockRecordPO {
    private Long id;
    private Long warehouseId;
    private Long materialId;
    private Integer type;
    private double quantity;
    private Long purchaseRequestId;
    private Long projectId;
    private Long operatorId;
    private LocalDateTime createdAt;
    private String purchaseRequestCode;
    private String operatorName;
}
