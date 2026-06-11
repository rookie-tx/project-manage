package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class StockRecord {
    private Long id;
    private Long warehouseId;
    private Long materialId;
    private StockType type;
    private double quantity;
    private Long purchaseRequestId;
    private Long projectId;
    private Long operatorId;
    private LocalDateTime createdAt;

    private StockRecord() {}

    public static StockRecord fromPersistence(Long id, Long warehouseId, Long materialId,
                                               int typeCode, double quantity,
                                               Long purchaseRequestId, Long projectId,
                                               Long operatorId, LocalDateTime createdAt) {
        StockRecord sr = new StockRecord();
        sr.id = id;
        sr.warehouseId = warehouseId;
        sr.materialId = materialId;
        sr.type = StockType.fromCode(typeCode);
        sr.quantity = quantity;
        sr.purchaseRequestId = purchaseRequestId;
        sr.projectId = projectId;
        sr.operatorId = operatorId;
        sr.createdAt = createdAt;
        return sr;
    }
}
