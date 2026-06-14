package cn.tx.aiagent.supplier.infrastructure;

import cn.tx.aiagent.supplier.domain.Supplier;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SupplierRepositoryImpl implements SupplierRepository {

    private final SupplierMapper mapper;
    private final SupplierPoMapper poMapper;

    public SupplierRepositoryImpl(SupplierMapper mapper, SupplierPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public List<Supplier> findAll(String keyword, Integer statusCode, int offset, int limit) {
        return mapper.selectAll(keyword, statusCode, offset, limit).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public long count(String keyword, Integer statusCode) {
        return mapper.count(keyword, statusCode);
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        SupplierPO po = mapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<Supplier> findByUserId(Long userId) {
        SupplierPO po = mapper.selectByUserId(userId);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public void save(Supplier supplier) {
        SupplierPO po = poMapper.toPO(supplier);
        mapper.insert(po);
        supplier.setId(po.getId());
    }

    @Override
    public void update(Supplier supplier) {
        mapper.update(poMapper.toPO(supplier));
    }

    @Override
    public void softDelete(Long id) {
        mapper.softDelete(id);
    }
}
