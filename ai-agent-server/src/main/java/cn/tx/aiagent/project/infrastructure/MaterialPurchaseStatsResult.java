package cn.tx.aiagent.project.infrastructure;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 物料采购统计查询结果，用于 MyBatis 映射统计 SQL 的聚合数据。
 */
@Data
public class MaterialPurchaseStatsResult {
    private Long materialId;
    private String materialName;
    private String materialSpec;
    private String materialUnit;
    private Double totalQuantity;
    private BigDecimal totalAmount;
}
