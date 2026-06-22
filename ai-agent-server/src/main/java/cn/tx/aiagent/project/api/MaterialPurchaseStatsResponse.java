package cn.tx.aiagent.project.api;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 物料采购统计响应 DTO。
 */
@Data
public class MaterialPurchaseStatsResponse {
    private Long materialId;
    private String materialName;
    private String materialSpec;
    private String materialUnit;
    private Double totalQuantity;
    private BigDecimal totalAmount;
}
