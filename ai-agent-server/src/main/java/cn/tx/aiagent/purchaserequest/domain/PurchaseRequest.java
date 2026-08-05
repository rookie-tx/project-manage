package cn.tx.aiagent.purchaserequest.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class PurchaseRequest {

    private Long id;
    private String code;
    private Long projectId;
    private Long materialId;
    private String name;
    private String specification;
    private String brand;
    private BigDecimal quantity;
    private String unit;
    private LocalDate arrivalDate;
    private PurchaseRequestStatus status;
    private String remark;
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PurchaseRequest() {}

    public static PurchaseRequest create(Long projectId, Long materialId, String name, String specification,
                                          String brand, BigDecimal quantity, String unit,
                                          LocalDate arrivalDate, String remark, Long createdBy) {
        PurchaseRequest pr = new PurchaseRequest();
        pr.projectId = projectId;
        pr.materialId = materialId;
        pr.name = name;
        pr.specification = specification;
        pr.brand = brand;
        pr.quantity = quantity;
        pr.unit = unit;
        pr.arrivalDate = arrivalDate;
        pr.remark = remark;
        pr.status = PurchaseRequestStatus.PENDING;
        pr.isDeleted = 0;
        pr.createdBy = createdBy;
        return pr;
    }

    public static PurchaseRequest fromPersistence(Long id, String code, Long projectId, Long materialId,
                                                   String name, String specification, String brand,
                                                   BigDecimal quantity, String unit,
                                                   LocalDate arrivalDate,
                                                   PurchaseRequestStatus status, String remark,
                                                   Integer isDeleted, Long createdBy,
                                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        PurchaseRequest pr = new PurchaseRequest();
        pr.id = id;
        pr.code = code;
        pr.projectId = projectId;
        pr.materialId = materialId;
        pr.name = name;
        pr.specification = specification;
        pr.brand = brand;
        pr.quantity = quantity;
        pr.unit = unit;
        pr.arrivalDate = arrivalDate;
        pr.status = status;
        pr.remark = remark;
        pr.isDeleted = isDeleted;
        pr.createdBy = createdBy;
        pr.createdAt = createdAt;
        pr.updatedAt = updatedAt;
        return pr;
    }

    public void update(Long materialId, String name, String specification, String brand,
                       BigDecimal quantity, String unit, LocalDate arrivalDate,
                       String remark) {
        if (materialId != null) this.materialId = materialId;
        if (name != null) this.name = name;
        if (specification != null) this.specification = specification;
        if (brand != null) this.brand = brand;
        if (quantity != null) this.quantity = quantity;
        if (unit != null) this.unit = unit;
        if (arrivalDate != null) this.arrivalDate = arrivalDate;
        if (remark != null) this.remark = remark;
    }

    public void setId(Long id) { this.id = id; }

    public void setCode(String code) { this.code = code; }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void markDeleted() {
        this.isDeleted = 1;
    }

    public void setStatus(PurchaseRequestStatus status) {
        this.status = status;
    }
}
