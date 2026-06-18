package cn.tx.aiagent.quotation.application;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报价响应 DTO
 */
@Data
public class QuotationResponse {

    private Long id;
    private Long purchaseOrderId;
    private Long supplierId;

    /** 供应商信息（应用层填充） */
    private String supplierName;
    private String supplierCompanyName;
    private String supplierContactPerson;
    private String supplierContactPhone;

    /** 报价信息 */
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate deliveryDate;
    private String remark;

    /** 状态 */
    private Integer statusCode;
    private String statusLabel;

    /** 创建信息 */
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联采购单摘要 */
    private PurchaseOrderSummary purchaseOrderInfo;

    @Data
    public static class PurchaseOrderSummary {
        private Long id;
        private String materialName;
        private String materialSpec;
        private String quantityStr;
        private String projectName;
        private LocalDate expectedArrivalDate;
    }
}
