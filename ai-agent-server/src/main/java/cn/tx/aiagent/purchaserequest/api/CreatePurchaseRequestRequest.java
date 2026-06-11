package cn.tx.aiagent.purchaserequest.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePurchaseRequestRequest {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotBlank(message = "物料名称不能为空")
    private String name;

    private String specification;

    private String brand;

    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @NotNull(message = "单位不能为空")
    private Integer unit;

    @NotNull(message = "到场时间不能为空")
    private LocalDate arrivalDate;

    private String remark;
}
