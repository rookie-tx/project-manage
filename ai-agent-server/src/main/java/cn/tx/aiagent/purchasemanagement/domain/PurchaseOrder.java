package cn.tx.aiagent.purchasemanagement.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PurchaseOrder {

    private Long id;
    private Long purchaseRequestId;
    private String supplierName;
    private String supplierPhone;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate purchaseDate;
    private LocalDate expectedArrivalDate;
    private PurchaseOrderStatus status;
    private String remark;
    private Integer isDeleted;
    private Long createdBy;
    private Long purchaserId;
    private Long inboundOperatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PurchaseOrder() {}

    /** 自动创建（审批通过时），基本信息留空 */
    public static PurchaseOrder createAuto(Long purchaseRequestId, LocalDate expectedArrivalDate, Long createdBy) {
        PurchaseOrder po = new PurchaseOrder();
        po.purchaseRequestId = purchaseRequestId;
        po.supplierName = null;
        po.supplierPhone = null;
        po.unitPrice = null;
        po.totalAmount = BigDecimal.ZERO;
        po.purchaseDate = null;
        po.expectedArrivalDate = expectedArrivalDate;
        po.status = PurchaseOrderStatus.PURCHASING;
        po.isDeleted = 0;
        po.createdBy = createdBy;
        po.purchaserId = null;
        po.inboundOperatorId = null;
        return po;
    }

    /** 手动创建（管理员用） */
    public static PurchaseOrder create(Long purchaseRequestId, String supplierName, String supplierPhone,
                                        BigDecimal unitPrice, BigDecimal totalAmount, LocalDate purchaseDate,
                                        LocalDate expectedArrivalDate, String remark, Long createdBy) {
        PurchaseOrder po = new PurchaseOrder();
        po.purchaseRequestId = purchaseRequestId;
        po.supplierName = supplierName;
        po.supplierPhone = supplierPhone;
        po.unitPrice = unitPrice;
        po.totalAmount = totalAmount;
        po.purchaseDate = purchaseDate;
        po.expectedArrivalDate = expectedArrivalDate;
        po.remark = remark;
        po.status = PurchaseOrderStatus.PURCHASING;
        po.isDeleted = 0;
        po.createdBy = createdBy;
        po.purchaserId = null;
        po.inboundOperatorId = null;
        return po;
    }

    public static PurchaseOrder fromPersistence(Long id, Long purchaseRequestId, String supplierName,
                                                  String supplierPhone, BigDecimal unitPrice, BigDecimal totalAmount,
                                                  LocalDate purchaseDate, LocalDate expectedArrivalDate,
                                                  int statusCode, String remark, Integer isDeleted,
                                                  Long createdBy, Long purchaserId, Long inboundOperatorId,
                                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        PurchaseOrder po = new PurchaseOrder();
        po.id = id;
        po.purchaseRequestId = purchaseRequestId;
        po.supplierName = supplierName;
        po.supplierPhone = supplierPhone;
        po.unitPrice = unitPrice;
        po.totalAmount = totalAmount;
        po.purchaseDate = purchaseDate;
        po.expectedArrivalDate = expectedArrivalDate;
        po.status = PurchaseOrderStatus.fromCode(statusCode);
        po.remark = remark;
        po.isDeleted = isDeleted;
        po.createdBy = createdBy;
        po.purchaserId = purchaserId;
        po.inboundOperatorId = inboundOperatorId;
        po.createdAt = createdAt;
        po.updatedAt = updatedAt;
        return po;
    }

    public void update(String supplierName, String supplierPhone, BigDecimal unitPrice,
                        BigDecimal totalAmount, LocalDate purchaseDate,
                        LocalDate expectedArrivalDate, String remark) {
        if (supplierName != null) this.supplierName = supplierName;
        if (supplierPhone != null) this.supplierPhone = supplierPhone;
        if (unitPrice != null) this.unitPrice = unitPrice;
        if (totalAmount != null) this.totalAmount = totalAmount;
        if (purchaseDate != null) this.purchaseDate = purchaseDate;
        if (expectedArrivalDate != null) this.expectedArrivalDate = expectedArrivalDate;
        if (remark != null) this.remark = remark;
    }

    public boolean isSupplierFilled() {
        return supplierName != null && !supplierName.isEmpty();
    }

    public void changeStatus(PurchaseOrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalArgumentException("状态流转不合法: " + status.getLabel() + " → " + target.getLabel());
        }
        this.status = target;
    }

    public void setId(Long id) { this.id = id; }

    public void markDeleted() { this.isDeleted = 1; }

    public void setPurchaser(Long purchaserId) { this.purchaserId = purchaserId; }

    public void setInboundOperator(Long inboundOperatorId) { this.inboundOperatorId = inboundOperatorId; }
}
