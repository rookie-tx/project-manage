package cn.tx.aiagent.attendance.infrastructure;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendancePO {
    private Long id;
    private Long workerId;
    private Long projectId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private Integer status;
    private BigDecimal clockInLat;
    private BigDecimal clockInLng;
    private BigDecimal clockOutLat;
    private BigDecimal clockOutLng;
    private Integer locationRisk;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
