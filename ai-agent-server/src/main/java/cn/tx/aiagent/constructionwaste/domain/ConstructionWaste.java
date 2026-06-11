package cn.tx.aiagent.constructionwaste.domain;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ConstructionWaste {
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

    private ConstructionWaste() {}

    public static ConstructionWaste create(Long projectId, String wasteType, double quantity,
                                            String unit, String vehiclePlate, String driverName,
                                            String destination, LocalDate removalDate,
                                            String remark, Long operatorId) {
        ConstructionWaste w = new ConstructionWaste();
        w.projectId = projectId;
        w.wasteType = wasteType;
        w.quantity = quantity;
        w.unit = unit;
        w.vehiclePlate = vehiclePlate;
        w.driverName = driverName;
        w.destination = destination;
        w.removalDate = removalDate;
        w.remark = remark;
        w.operatorId = operatorId;
        w.isDeleted = 0;
        return w;
    }

    public static ConstructionWaste fromPersistence(Long id, Long projectId, String wasteType,
                                                     double quantity, String unit, String vehiclePlate,
                                                     String driverName, String destination,
                                                     LocalDate removalDate, String remark,
                                                     Long operatorId, int isDeleted,
                                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        ConstructionWaste w = new ConstructionWaste();
        w.id = id;
        w.projectId = projectId;
        w.wasteType = wasteType;
        w.quantity = quantity;
        w.unit = unit;
        w.vehiclePlate = vehiclePlate;
        w.driverName = driverName;
        w.destination = destination;
        w.removalDate = removalDate;
        w.remark = remark;
        w.operatorId = operatorId;
        w.isDeleted = isDeleted;
        w.createdAt = createdAt;
        w.updatedAt = updatedAt;
        return w;
    }

    public void markDeleted() { this.isDeleted = 1; }
    public boolean isDeleted() { return isDeleted != null && isDeleted == 1; }
}
