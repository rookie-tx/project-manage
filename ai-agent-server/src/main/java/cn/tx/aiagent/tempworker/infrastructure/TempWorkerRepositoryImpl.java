package cn.tx.aiagent.tempworker.infrastructure;

import cn.tx.aiagent.tempworker.domain.TempWorker;
import cn.tx.aiagent.tempworker.domain.TempWorkerRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class TempWorkerRepositoryImpl implements TempWorkerRepository {
    private final TempWorkerMapper mapper;
    private final TempWorkerPoMapper poMapper;

    public TempWorkerRepositoryImpl(TempWorkerMapper mapper, TempWorkerPoMapper poMapper) {
        this.mapper = mapper; this.poMapper = poMapper;
    }

    public List<TempWorker> findByProjectId(Long projectId) { return mapper.selectByProjectId(projectId).stream().map(poMapper::toDomain).toList(); }
    public Optional<TempWorker> findById(Long id) { TempWorkerPO po = mapper.selectById(id); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public Optional<TempWorker> findByUserId(Long userId) { TempWorkerPO po = mapper.selectByUserId(userId); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po)); }
    public TempWorker save(TempWorker w) { TempWorkerPO po = poMapper.toPO(w); mapper.insert(po); return poMapper.toDomain(po); }
    public void update(TempWorker w) { mapper.update(poMapper.toPO(w)); }
    public void softDelete(Long id) { mapper.softDelete(id); }
}
