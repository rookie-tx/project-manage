package cn.tx.aiagent.purchaserequest.infrastructure;

import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PurchaseRequestRepositoryImpl implements PurchaseRequestRepository {

    private final PurchaseRequestMapper mapper;
    private final PurchaseRequestPoMapper poMapper;

    public PurchaseRequestRepositoryImpl(PurchaseRequestMapper mapper, PurchaseRequestPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public void save(PurchaseRequest pr) {
        mapper.insert(poMapper.toPO(pr));
    }

    @Override
    public Optional<PurchaseRequest> findById(Long id) {
        PurchaseRequestPO po = mapper.selectById(id);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<PurchaseRequest> findByPage(Long projectId, Long createdBy, int offset, int size) {
        return mapper.selectByPage(projectId, createdBy, offset, size).stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public long countByProject(Long projectId, Long createdBy) {
        return mapper.countByProject(projectId, createdBy);
    }

    @Override
    public void update(PurchaseRequest pr) {
        mapper.update(poMapper.toPO(pr));
    }

    @Override
    public void softDelete(Long id) {
        mapper.softDelete(id);
    }

    @Override
    public void updateStatus(Long id, PurchaseRequestStatus status) {
        mapper.updateStatus(id, status.getCode());
    }

    @Override
    public String findMaxCodeByPrefix(String prefix) {
        return mapper.findMaxCodeByPrefix(prefix);
    }

    @Override
    public List<PurchaseRequest> findAvailable() {
        return mapper.selectAvailable().stream().map(poMapper::toDomain).toList();
    }

    @Override
    public boolean existsInPurchaseOrder(Long id) {
        return mapper.existsInPurchaseOrder(id);
    }
}
