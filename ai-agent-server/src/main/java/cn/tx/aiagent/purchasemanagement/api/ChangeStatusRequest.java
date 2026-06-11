package cn.tx.aiagent.purchasemanagement.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
