package cn.tx.aiagent.purchaserequest.infrastructure;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class PurchaseRequestPO {

    private Long id;
    private String code;
    private Long projectId;
    private Long materialId;
    private String name;
    private String specification;
    private String brand;
    private Double quantity;
    private String unit;
    private LocalDate arrivalDate;
    private Integer status;
    private String remark;
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
