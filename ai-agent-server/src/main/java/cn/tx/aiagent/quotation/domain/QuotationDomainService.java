package cn.tx.aiagent.quotation.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 报价领域服务。
 * 负责报价的提交、选用等核心业务逻辑。
 */
public class QuotationDomainService {

    private final QuotationRepository quotationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public QuotationDomainService(QuotationRepository quotationRepository,
                                   PurchaseOrderRepository purchaseOrderRepository) {
        this.quotationRepository = quotationRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * 供应商提交或修改报价。
     * 同一供应商对同一采购单只能有一条报价，重复提交则更新。
     */
    public Quotation submit(Long purchaseOrderId, Long supplierId,
                            BigDecimal unitPrice, BigDecimal totalAmount,
                            LocalDate deliveryDate, String remark, Long createdBy) {
        // 校验采购单存在且状态为 PURCHASING
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        if (po.getStatus() != PurchaseOrderStatus.PURCHASING) {
            throw new BusinessException(ErrorCode.QUOTATION_ORDER_STATUS_INVALID);
        }

        // 检查是否已有报价
        Optional<Quotation> existing = quotationRepository.findByOrderAndSupplier(purchaseOrderId, supplierId);
        if (existing.isPresent()) {
            Quotation q = existing.get();
            q.update(unitPrice, totalAmount, deliveryDate, remark);
            quotationRepository.update(q);
            return q;
        }

        // 创建新报价
        Quotation q = Quotation.create(purchaseOrderId, supplierId, unitPrice, totalAmount,
                deliveryDate, remark, createdBy);
        quotationRepository.save(q);
        return q;
    }

    /**
     * 管理员选用报价。
     * 原子操作：校验报价状态 → 更新报价为已选用 → 更新采购单供应商信息 → 采购单状态流转到已采购
     */
    public PurchaseOrder selectQuotation(Long quotationId, Long userId) {
        Quotation q = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTATION_NOT_FOUND));

        if (!q.isPending()) {
            throw new BusinessException(ErrorCode.QUOTATION_ALREADY_SELECTED);
        }

        PurchaseOrder po = purchaseOrderRepository.findById(q.getPurchaseOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));

        if (po.getStatus() != PurchaseOrderStatus.PURCHASING) {
            throw new BusinessException(ErrorCode.QUOTATION_ORDER_STATUS_INVALID);
        }

        // 更新报价状态
        q.markSelected();
        quotationRepository.updateStatus(q.getId(), QuotationStatus.SELECTED);

        // 用报价信息更新采购单（这里只更新价格信息，供应商信息需在应用层填充）
        po.update(null, null, q.getUnitPrice(), q.getTotalAmount(), null, q.getDeliveryDate(), q.getRemark());
        po.changeStatus(PurchaseOrderStatus.PURCHASED);
        po.setPurchaser(userId);

        purchaseOrderRepository.update(po);
        purchaseOrderRepository.updateStatus(po.getId(), PurchaseOrderStatus.PURCHASED);
        purchaseOrderRepository.updatePurchaser(po.getId(), userId);

        return po;
    }
}
