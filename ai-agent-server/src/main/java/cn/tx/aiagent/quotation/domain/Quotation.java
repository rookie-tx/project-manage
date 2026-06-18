package cn.tx.aiagent.quotation.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报价单领域实体。
 * 供应商对采购单的报价，同一供应商对同一采购单只能有一条有效报价。
 */
@Getter
public class Quotation {

    private Long id;
    private Long purchaseOrderId;
    private Long supplierId;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate deliveryDate;
    private String remark;
    private QuotationStatus status;
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Quotation() {}

    /** 创建新报价 */
    public static Quotation create(Long purchaseOrderId, Long supplierId,
                                   BigDecimal unitPrice, BigDecimal totalAmount,
                                   LocalDate deliveryDate, String remark, Long createdBy) {
        Quotation q = new Quotation();
        q.purchaseOrderId = purchaseOrderId;
        q.supplierId = supplierId;
        q.unitPrice = unitPrice;
        q.totalAmount = totalAmount;
        q.deliveryDate = deliveryDate;
        q.remark = remark;
        q.status = QuotationStatus.PENDING;
        q.isDeleted = 0;
        q.createdBy = createdBy;
        return q;
    }

    /** 从持久层还原 */
    public static Quotation fromPersistence(Long id, Long purchaseOrderId, Long supplierId,
                                             BigDecimal unitPrice, BigDecimal totalAmount,
                                             LocalDate deliveryDate, String remark,
                                             int statusCode, Integer isDeleted,
                                             Long createdBy, LocalDateTime createdAt,
                                             LocalDateTime updatedAt) {
        Quotation q = new Quotation();
        q.id = id;
        q.purchaseOrderId = purchaseOrderId;
        q.supplierId = supplierId;
        q.unitPrice = unitPrice;
        q.totalAmount = totalAmount;
        q.deliveryDate = deliveryDate;
        q.remark = remark;
        q.status = QuotationStatus.fromCode(statusCode);
        q.isDeleted = isDeleted;
        q.createdBy = createdBy;
        q.createdAt = createdAt;
        q.updatedAt = updatedAt;
        return q;
    }

    /** 更新报价内容（单价、总金额、交货日期、备注），状态重置为待审核 */
    public void update(BigDecimal unitPrice, BigDecimal totalAmount,
                       LocalDate deliveryDate, String remark) {
        if (unitPrice != null) this.unitPrice = unitPrice;
        if (totalAmount != null) this.totalAmount = totalAmount;
        if (deliveryDate != null) this.deliveryDate = deliveryDate;
        if (remark != null) this.remark = remark;
        this.status = QuotationStatus.PENDING;
    }

    /** 设置为已选用 */
    public void markSelected() {
        this.status = QuotationStatus.SELECTED;
    }

    /** 设置 ID（MyBatis insert 后回填） */
    public void setId(Long id) {
        this.id = id;
    }

    /** 软删除 */
    public void markDeleted() {
        this.isDeleted = 1;
    }

    /** 是否待审核 */
    public boolean isPending() {
        return status == QuotationStatus.PENDING;
    }

    /** 是否已被选用 */
    public boolean isSelected() {
        return status == QuotationStatus.SELECTED;
    }
}
