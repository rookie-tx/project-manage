package cn.tx.aiagent.purchaserequest.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePurchaseRequestRequest {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotNull(message = "物料不能为空")
    private Long materialId;

    private String brand;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @NotNull(message = "到场时间不能为空")
    private LocalDate arrivalDate;

    private String remark;
}
