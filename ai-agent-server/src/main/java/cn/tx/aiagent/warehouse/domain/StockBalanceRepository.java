package cn.tx.aiagent.warehouse.domain;

import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository {
    Optional<StockBalance> findByWarehouseAndMaterial(Long warehouseId, Long materialId);
    List<StockBalance> findByWarehouseId(Long warehouseId);
    List<StockBalance> findByMaterialId(Long materialId);
    void upsert(Long warehouseId, Long materialId, double delta);
}
