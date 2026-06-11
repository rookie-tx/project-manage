package cn.tx.aiagent.attendancearea.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打卡区域领域实体。
 * 使用私有构造器，通过静态工厂方法创建实例：
 * <ul>
 *   <li>{@link #create} — 新建打卡区域</li>
 *   <li>{@link #fromPersistence} — 从持久层还原</li>
 * </ul>
 */
@Getter
public class AttendanceArea {

    private Long id;
    private Long projectId;
    private String name;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private Integer radius;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AttendanceArea() {
    }

    /** 创建新打卡区域 */
    public static AttendanceArea create(Long projectId, String name,
                                         BigDecimal centerLat, BigDecimal centerLng, Integer radius) {
        AttendanceArea area = new AttendanceArea();
        area.projectId = projectId;
        area.name = name;
        area.centerLat = centerLat;
        area.centerLng = centerLng;
        area.radius = radius;
        area.isDeleted = 0;
        return area;
    }

    /** 从持久层数据还原 */
    public static AttendanceArea fromPersistence(Long id, Long projectId, String name,
                                                   BigDecimal centerLat, BigDecimal centerLng,
                                                   Integer radius, int isDeleted,
                                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        AttendanceArea area = new AttendanceArea();
        area.id = id;
        area.projectId = projectId;
        area.name = name;
        area.centerLat = centerLat;
        area.centerLng = centerLng;
        area.radius = radius;
        area.isDeleted = isDeleted;
        area.createdAt = createdAt;
        area.updatedAt = updatedAt;
        return area;
    }

    /** 更新区域信息 */
    public void update(String name, BigDecimal centerLat, BigDecimal centerLng, Integer radius) {
        this.name = name;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.radius = radius;
    }

    /** 软删除标记 */
    public void markDeleted() {
        this.isDeleted = 1;
    }

    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
}
