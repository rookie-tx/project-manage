package cn.tx.aiagent.quotation.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 提交报价请求 DTO
 */
@Data
public class SubmitQuotationRequest {

    /** 采购单ID */
    @NotNull(message = "采购单ID不能为空")
    private Long purchaseOrderId;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 总金额 */
    @NotNull(message = "总金额不能为空")
    private BigDecimal totalAmount;

    /** 预计交货日期 */
    private LocalDate deliveryDate;

    /** 备注 */
    private String remark;
}
