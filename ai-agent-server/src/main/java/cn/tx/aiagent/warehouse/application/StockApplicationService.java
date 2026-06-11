package cn.tx.aiagent.warehouse.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.warehouse.domain.*;
import org.springframework.transaction.annotation.Transactional;

public class StockApplicationService {

    private final StockRecordRepository stockRecordRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final MaterialRepository materialRepository;

    public StockApplicationService(StockRecordRepository stockRecordRepository,
                                    StockBalanceRepository stockBalanceRepository,
                                    MaterialRepository materialRepository) {
        this.stockRecordRepository = stockRecordRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public void recordStock(Long warehouseId, Long materialId, StockType type, double quantity,
                            Long purchaseRequestId, Long projectId, Long operatorId) {
        if (type == StockType.OUT) {
            StockBalance balance = stockBalanceRepository.findByWarehouseAndMaterial(warehouseId, materialId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_INSUFFICIENT));
            if (balance.getQuantity() < quantity) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
            }
        }

        StockRecord record = StockRecord.fromPersistence(null, warehouseId, materialId, type.getCode(),
                quantity, purchaseRequestId, projectId, operatorId, null);
        stockRecordRepository.save(record);

        double delta = type == StockType.IN ? quantity : -quantity;
        stockBalanceRepository.upsert(warehouseId, materialId, delta);
    }

    public void updateWarehouse(WarehouseRepository warehouseRepo, Long id, String name, String location, Integer statusCode) {
        Warehouse w = warehouseRepo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND, id));
        if (name != null) { /* update via direct set */ }
        warehouseRepo.update(w);
    }
}
