package cn.tx.aiagent.purchaserequest.domain;

public class PurchaseRequestDomainService {

    private final PurchaseRequestRepository purchaseRequestRepository;

    public PurchaseRequestDomainService(PurchaseRequestRepository purchaseRequestRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
    }

    public PurchaseRequest create(PurchaseRequest pr) {
        purchaseRequestRepository.save(pr);
        return pr;
    }

    public void update(PurchaseRequest pr) {
        purchaseRequestRepository.update(pr);
    }

    public void updateStatus(PurchaseRequest pr, PurchaseRequestStatus newStatus) {
        purchaseRequestRepository.updateStatus(pr.getId(), newStatus);
    }

    public void softDelete(PurchaseRequest pr) {
        pr.markDeleted();
        purchaseRequestRepository.softDelete(pr.getId());
    }
}
