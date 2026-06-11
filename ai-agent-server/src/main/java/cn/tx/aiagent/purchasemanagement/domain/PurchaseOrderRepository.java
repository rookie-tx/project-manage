package cn.tx.aiagent.purchasemanagement.domain;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository {

    void save(PurchaseOrder po);

    Optional<PurchaseOrder> findById(Long id);

    List<PurchaseOrder> findByPage(Long projectId, Integer status, int offset, int size);

    long countByFilter(Long projectId, Integer status);

    void update(PurchaseOrder po);

    void softDelete(Long id);

    void updateStatus(Long id, PurchaseOrderStatus status);

    boolean existsByPurchaseRequestId(Long purchaseRequestId);

    void insertItem(Long orderId, Long requestId);

    List<Long> findRequestIdsByOrderId(Long orderId);

    void deleteItemsByOrderId(Long orderId);
}
