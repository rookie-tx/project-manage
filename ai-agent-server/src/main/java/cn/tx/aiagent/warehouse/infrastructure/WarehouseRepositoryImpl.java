package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.Warehouse;
import cn.tx.aiagent.warehouse.domain.WarehouseRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class WarehouseRepositoryImpl implements WarehouseRepository {
    private final WarehouseMapper mapper;
    private final WarehousePoMapper poMapper;

    public WarehouseRepositoryImpl(WarehouseMapper mapper, WarehousePoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    public List<Warehouse> findAll() { return mapper.selectAll().stream().map(poMapper::toDomain).toList(); }
    public Optional<Warehouse> findById(Long id) { WarehousePO po = mapper.selectById(id); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public Optional<Warehouse> findByCode(String code) { WarehousePO po = mapper.selectByCode(code); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public Warehouse save(Warehouse w) { WarehousePO po = poMapper.toPO(w); mapper.insert(po); return poMapper.toDomain(po); }
    public void update(Warehouse w) { mapper.update(poMapper.toPO(w)); }
    public void softDelete(Long id) { mapper.softDelete(id); }
}
