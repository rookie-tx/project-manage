package cn.tx.aiagent.purchasemanagement.infrastructure;

import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    private final PurchaseOrderMapper mapper;
    private final PurchaseOrderPoMapper poMapper;

    public PurchaseOrderRepositoryImpl(PurchaseOrderMapper mapper, PurchaseOrderPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public void save(PurchaseOrder po) {
        PurchaseOrderPO poObj = poMapper.toPO(po);
        mapper.insert(poObj);
        po.setId(poObj.getId());
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        PurchaseOrderPO po = mapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<PurchaseOrder> findByPage(Long projectId, Integer status, String tab, int offset, int size) {
        return mapper.selectByPage(projectId, status, tab, offset, size).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public long countByFilter(Long projectId, Integer status, String tab) {
        return mapper.countByFilter(projectId, status, tab);
    }

    @Override
    public Optional<PurchaseOrder> findByPRId(Long prId) {
        PurchaseOrderPO po = mapper.selectByPRId(prId);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<PurchaseOrder> findByStatus(Integer status) {
        return mapper.selectByStatus(status).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public void update(PurchaseOrder po) { mapper.update(poMapper.toPO(po)); }

    @Override
    public void softDelete(Long id) { mapper.softDelete(id); }

    @Override
    public void updateStatus(Long id, PurchaseOrderStatus status) {
        mapper.updateStatus(id, status.getCode());
    }

    @Override
    public void updatePurchaser(Long id, Long purchaserId) {
        mapper.updatePurchaser(id, purchaserId);
    }

    @Override
    public void updateInboundOperator(Long id, Long inboundOperatorId) {
        mapper.updateInboundOperator(id, inboundOperatorId);
    }

    @Override
    public boolean existsByPurchaseRequestId(Long purchaseRequestId) {
        return mapper.existsByPurchaseRequestId(purchaseRequestId);
    }
}
