package cn.tx.aiagent.warehouse.domain;

import java.util.List;

public interface StockRecordRepository {
    StockRecord save(StockRecord record);
    List<StockRecord> findAll(Long warehouseId, Long materialId, Integer type, int offset, int limit);
    long count(Long warehouseId, Long materialId, Integer type);
}
