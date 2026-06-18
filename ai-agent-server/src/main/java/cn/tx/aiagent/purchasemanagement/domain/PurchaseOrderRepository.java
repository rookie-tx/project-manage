package cn.tx.aiagent.purchasemanagement.domain;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {

    void save(PurchaseOrder po);

    Optional<PurchaseOrder> findById(Long id);

    List<PurchaseOrder> findByPage(Long projectId, Integer status, String tab, int offset, int size);

    long countByFilter(Long projectId, Integer status, String tab);

    Optional<PurchaseOrder> findByPRId(Long prId);

    List<PurchaseOrder> findByStatus(Integer status);

    void update(PurchaseOrder po);

    void softDelete(Long id);

    void updateStatus(Long id, PurchaseOrderStatus status);

    void updatePurchaser(Long id, Long purchaserId);

    void updateInboundOperator(Long id, Long inboundOperatorId);

    boolean existsByPurchaseRequestId(Long purchaseRequestId);
}
