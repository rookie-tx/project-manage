package cn.tx.aiagent.purchaserequest.api;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdatePurchaseRequestRequest {

    private Long projectId;

    private String name;

    private String specification;

    private String brand;

    private BigDecimal quantity;

    private Integer unit;

    private LocalDate arrivalDate;

    private Integer status;

    private String remark;
}
