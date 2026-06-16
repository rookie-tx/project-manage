package cn.tx.aiagent.purchaserequest.api;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PurchaseRequestResponse {

    private Long id;
    private String code;
    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long materialId;
    private String materialName;
    private String materialSpec;
    private String materialUnit;
    private String name;
    private String specification;
    private String brand;
    private BigDecimal quantity;
    private Integer unitCode;
    private String unitLabel;
    private LocalDate arrivalDate;
    private Integer statusCode;
    private String statusLabel;
    private String remark;
    private Long createdBy;
    private String createdByName;
    private String currentApprovalNode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
