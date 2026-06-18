package cn.tx.aiagent.quotation.domain;

import java.util.List;
import java.util.Optional;

/**
 * 报价仓储接口
 */
public interface QuotationRepository {

    void save(Quotation quotation);

    Optional<Quotation> findById(Long id);

    /** 按采购单ID和供应商ID查询（用于检查是否已报价） */
    Optional<Quotation> findByOrderAndSupplier(Long purchaseOrderId, Long supplierId);

    /** 按采购单ID分页查询所有报价（管理端） */
    List<Quotation> findByPage(Long purchaseOrderId, int offset, int size);

    /** 统计某采购单的报价数量 */
    long countByPurchaseOrder(Long purchaseOrderId);

    /** 按创建人（供应商用户ID）分页查询 */
    List<Quotation> findByCreatedBy(Long createdBy, int offset, int size);

    /** 统计某供应商的报价数量 */
    long countByCreatedBy(Long createdBy);

    /** 更新报价 */
    void update(Quotation quotation);

    /** 更新报价状态 */
    void updateStatus(Long id, QuotationStatus status);

    /** 软删除 */
    void softDelete(Long id);
}
