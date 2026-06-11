package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;

@Getter
public class StockBalance {
    private Long id;
    private Long warehouseId;
    private Long materialId;
    private double quantity;

    private StockBalance() {}

    public static StockBalance fromPersistence(Long id, Long warehouseId, Long materialId, double quantity) {
        StockBalance sb = new StockBalance();
        sb.id = id;
        sb.warehouseId = warehouseId;
        sb.materialId = materialId;
        sb.quantity = quantity;
        return sb;
    }
}
