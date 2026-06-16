package cn.tx.aiagent.purchaserequest.api;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdatePurchaseRequestRequest {

    private Long projectId;

    private Long materialId;

    private String brand;

    private BigDecimal quantity;

    private LocalDate arrivalDate;

    private Integer status;

    private String remark;
}
