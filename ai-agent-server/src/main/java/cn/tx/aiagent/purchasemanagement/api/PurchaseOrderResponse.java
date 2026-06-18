package cn.tx.aiagent.purchasemanagement.api;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderResponse {
    private Long id;
    private Long purchaseRequestId;
    private String purchaseRequestName;
    private String projectName;
    private List<PurchaseRequestInfo> purchaseRequests;
    private String supplierName;
    private String supplierPhone;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate purchaseDate;
    private LocalDate expectedArrivalDate;
    private Integer statusCode;
    private String statusLabel;
    private String remark;
    private Long createdBy;
    private String createdByName;
    private Long purchaserId;
    private String purchaserName;
    private Long inboundOperatorId;
    private String inboundOperatorName;
    private List<FileInfo> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class PurchaseRequestInfo {
        private Long id;
        private String code;
        private String name;
        private String specification;
        private String quantityStr;
        private String arrivalDate;
        private String projectName;
        private Long materialId;
        private String materialName;
        private String materialSpec;
    }

    @Data
    public static class FileInfo {
        private Long id;
        private String originalName;
        private String contentType;
        private String url;
    }
}
