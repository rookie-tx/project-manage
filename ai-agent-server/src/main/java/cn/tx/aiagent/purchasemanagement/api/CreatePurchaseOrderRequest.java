package cn.tx.aiagent.purchasemanagement.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePurchaseOrderRequest {

    @NotNull(message = "采购申请不能为空")
    private Long purchaseRequestId;
}
