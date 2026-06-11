package cn.tx.aiagent.warehouse.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockBalancePO {
    private Long id;
    private Long warehouseId;
    private Long materialId;
    private double quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
