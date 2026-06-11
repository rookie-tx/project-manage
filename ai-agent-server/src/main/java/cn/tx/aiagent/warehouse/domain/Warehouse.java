package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Warehouse {
    private Long id;
    private String code;
    private String name;
    private String location;
    private WarehouseStatus status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Warehouse() {}

    public static Warehouse create(String code, String name, String location) {
        Warehouse w = new Warehouse();
        w.code = code;
        w.name = name;
        w.location = location;
        w.status = WarehouseStatus.ACTIVE;
        w.isDeleted = 0;
        return w;
    }

    public static Warehouse fromPersistence(Long id, String code, String name, String location,
                                            int statusCode, int isDeleted,
                                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        Warehouse w = new Warehouse();
        w.id = id;
        w.code = code;
        w.name = name;
        w.location = location;
        w.status = WarehouseStatus.fromCode(statusCode);
        w.isDeleted = isDeleted;
        w.createdAt = createdAt;
        w.updatedAt = updatedAt;
        return w;
    }

    public void markDeleted() { this.isDeleted = 1; }
    public boolean isDeleted() { return isDeleted != null && isDeleted == 1; }
}
