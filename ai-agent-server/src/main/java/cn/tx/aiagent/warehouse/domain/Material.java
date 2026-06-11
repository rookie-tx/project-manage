package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class Material {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Material() {}

    public static Material create(String code, String name, String spec, String unit) {
        Material m = new Material();
        m.code = code;
        m.name = name;
        m.spec = spec;
        m.unit = unit;
        m.isDeleted = 0;
        return m;
    }

    public static Material fromPersistence(Long id, String code, String name, String spec, String unit,
                                           int isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Material m = new Material();
        m.id = id;
        m.code = code;
        m.name = name;
        m.spec = spec;
        m.unit = unit;
        m.isDeleted = isDeleted;
        m.createdAt = createdAt;
        m.updatedAt = updatedAt;
        return m;
    }

    public void markDeleted() { this.isDeleted = 1; }
    public boolean isDeleted() { return isDeleted != null && isDeleted == 1; }
}
