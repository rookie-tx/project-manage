package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.Material;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class MaterialRepositoryImpl implements MaterialRepository {
    private final MaterialMapper mapper;
    private final MaterialPoMapper poMapper;

    public MaterialRepositoryImpl(MaterialMapper mapper, MaterialPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    public List<Material> findAll() { return mapper.selectAll().stream().map(poMapper::toDomain).toList(); }
    public Optional<Material> findById(Long id) { MaterialPO po = mapper.selectById(id); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public Optional<Material> findByCode(String code) { MaterialPO po = mapper.selectByCode(code); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public Material save(Material m) { MaterialPO po = poMapper.toPO(m); mapper.insert(po); return poMapper.toDomain(po); }
    public void update(Material m) { mapper.update(poMapper.toPO(m)); }
    public void softDelete(Long id) { mapper.softDelete(id); }

    public List<Material> findByNameOrSpecLike(String keyword) {
        return mapper.selectByNameOrSpecLike(keyword).stream()
                .map(poMapper::toDomain).toList();
    }
}
