package cn.tx.aiagent.attendancearea.infrastructure;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceAreaPO {
    private Long id;
    private Long projectId;
    private String name;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private Integer radius;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
