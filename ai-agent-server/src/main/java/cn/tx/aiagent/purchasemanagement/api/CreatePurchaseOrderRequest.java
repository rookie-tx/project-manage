package cn.tx.aiagent.purchasemanagement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseOrderRequest {

    @NotNull(message = "采购申请不能为空")
    @Size(min = 1, message = "至少选择一个采购申请")
    private List<Long> purchaseRequestIds;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String supplierPhone;

    @NotNull(message = "总金额不能为空")
    @Positive(message = "总金额必须大于0")
    private BigDecimal totalAmount;

    private BigDecimal unitPrice;

    @NotNull(message = "采购日期不能为空")
    private LocalDate purchaseDate;

    private LocalDate expectedArrivalDate;

    private String remark;

    @NotNull(message = "附件不能为空")
    @Size(min = 1, message = "至少需要上传一个附件")
    private List<Long> fileIds;
}
