package cn.tx.aiagent.purchasemanagement.api;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePurchaseOrderRequest {
    private String supplierName;
    private String supplierPhone;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate purchaseDate;
    private LocalDate expectedArrivalDate;
    private String remark;

    private List<Long> purchaseRequestIds;

    private List<Long> fileIds;
}
