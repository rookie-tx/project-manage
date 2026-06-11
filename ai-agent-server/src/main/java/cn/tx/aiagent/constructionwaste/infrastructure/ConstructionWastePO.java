package cn.tx.aiagent.constructionwaste.infrastructure;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ConstructionWastePO {
    private Long id;
    private Long projectId;
    private String wasteType;
    private double quantity;
    private String unit;
    private String vehiclePlate;
    private String driverName;
    private String destination;
    private LocalDate removalDate;
    private String remark;
    private Long operatorId;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
