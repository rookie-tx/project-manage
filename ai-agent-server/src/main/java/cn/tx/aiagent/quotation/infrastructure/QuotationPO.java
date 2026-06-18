package cn.tx.aiagent.quotation.infrastructure;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报价持久化对象
 */
@Data
public class QuotationPO {
    private Long id;
    private Long purchaseOrderId;
    private Long supplierId;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private LocalDate deliveryDate;
    private String remark;
    private Integer status;
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
