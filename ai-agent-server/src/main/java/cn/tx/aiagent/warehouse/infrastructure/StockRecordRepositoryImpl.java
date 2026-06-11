package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.StockRecord;
import cn.tx.aiagent.warehouse.domain.StockRecordRepository;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StockRecordRepositoryImpl implements StockRecordRepository {
    private final StockRecordMapper mapper;
    private final StockRecordPoMapper poMapper;

    public StockRecordRepositoryImpl(StockRecordMapper mapper, StockRecordPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    public StockRecord save(StockRecord sr) {
        StockRecordPO po = poMapper.toPO(sr);
        mapper.insert(po);
        return poMapper.toDomain(mapper.selectById(po.getId()));
    }
    public List<StockRecord> findAll(Long warehouseId, Long materialId, Integer type, int offset, int limit) {
        return mapper.selectAll(warehouseId, materialId, type, offset, limit).stream().map(poMapper::toDomain).toList();
    }
    public long count(Long warehouseId, Long materialId, Integer type) { return mapper.count(warehouseId, materialId, type); }
}
