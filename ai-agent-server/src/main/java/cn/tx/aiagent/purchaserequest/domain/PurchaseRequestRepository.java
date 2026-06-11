package cn.tx.aiagent.purchaserequest.domain;

import java.util.List;
import java.util.Optional;

public interface PurchaseRequestRepository {

    void save(PurchaseRequest pr);

    Optional<PurchaseRequest> findById(Long id);

    List<PurchaseRequest> findByPage(Long projectId, Long createdBy, int offset, int size);

    long countByProject(Long projectId, Long createdBy);

    void update(PurchaseRequest pr);

    void softDelete(Long id);

    void updateStatus(Long id, PurchaseRequestStatus status);

    String findMaxCodeByPrefix(String prefix);

    List<PurchaseRequest> findAvailable();

    boolean existsInPurchaseOrder(Long id);
}
