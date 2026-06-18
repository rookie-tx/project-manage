package cn.tx.aiagent.purchasemanagement.infrastructure;

import lombok.Data;
import java.time.LocalDate;

/**
 * 待报价采购单查询结果（JOIN 采购申请、项目、物料）
 */
@Data
public class PendingOrderResult {
    private Long id;
    private LocalDate expectedArrivalDate;
    private Integer statusCode;
    private String materialName;
    private String specification;
    private Double quantity;
    private String unit;
    private String brand;
    private String projectName;
    private String materialName2;
}
