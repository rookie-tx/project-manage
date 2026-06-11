package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.StockBalance;
import cn.tx.aiagent.warehouse.domain.StockBalanceRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class StockBalanceRepositoryImpl implements StockBalanceRepository {
    private final StockBalanceMapper mapper;
    private final StockBalancePoMapper poMapper;

    public StockBalanceRepositoryImpl(StockBalanceMapper mapper, StockBalancePoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    public Optional<StockBalance> findByWarehouseAndMaterial(Long warehouseId, Long materialId) {
        StockBalancePO po = mapper.selectByWarehouseAndMaterial(warehouseId, materialId);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }
    public List<StockBalance> findByWarehouseId(Long warehouseId) { return mapper.selectByWarehouseId(warehouseId).stream().map(poMapper::toDomain).toList(); }
    public List<StockBalance> findByMaterialId(Long materialId) { return mapper.selectByMaterialId(materialId).stream().map(poMapper::toDomain).toList(); }
    public void upsert(Long warehouseId, Long materialId, double delta) { mapper.upsert(warehouseId, materialId, delta); }
}
