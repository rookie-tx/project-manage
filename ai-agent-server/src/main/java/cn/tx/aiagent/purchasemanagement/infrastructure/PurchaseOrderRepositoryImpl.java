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
    public List<PurchaseOrder> findByPage(Long projectId, Integer status, int offset, int size) {
        return mapper.selectByPage(projectId, status, offset, size).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public long countByFilter(Long projectId, Integer status) {
        return mapper.countByFilter(projectId, status);
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
    public boolean existsByPurchaseRequestId(Long purchaseRequestId) {
        return mapper.existsByPurchaseRequestId(purchaseRequestId);
    }

    @Override
    public void insertItem(Long orderId, Long requestId) {
        mapper.insertItem(orderId, requestId);
    }

    @Override
    public List<Long> findRequestIdsByOrderId(Long orderId) {
        return mapper.selectRequestIdsByOrderId(orderId);
    }

    @Override
    public void deleteItemsByOrderId(Long orderId) {
        mapper.deleteItemsByOrderId(orderId);
    }
}
