package cn.tx.aiagent.attendance.domain;

import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Attendance {
    private Long id;
    private Long workerId;
    private Long projectId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private AttendanceStatus status;
    private BigDecimal clockInLat;
    private BigDecimal clockInLng;
    private BigDecimal clockOutLat;
    private BigDecimal clockOutLng;
    private Integer locationRisk;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Attendance() {}

    /** 新建上班打卡，lat/lng 为 null 时跳过位置记录（兼容 Web 端） */
    public static Attendance create(Long workerId, Long projectId, Double lat, Double lng, int locationRisk) {
        Attendance a = new Attendance();
        a.workerId = workerId;
        a.projectId = projectId;
        a.clockIn = LocalDateTime.now();
        a.status = AttendanceStatus.CLOCKED_IN;
        if (lat != null) a.clockInLat = BigDecimal.valueOf(lat);
        if (lng != null) a.clockInLng = BigDecimal.valueOf(lng);
        a.locationRisk = locationRisk;
        return a;
    }

    /** 从持久层还原 */
    public static Attendance fromPersistence(Long id, Long workerId, Long projectId,
                                              LocalDateTime clockIn, LocalDateTime clockOut,
                                              int statusCode,
                                              BigDecimal clockInLat, BigDecimal clockInLng,
                                              BigDecimal clockOutLat, BigDecimal clockOutLng,
                                              int locationRisk,
                                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        Attendance a = new Attendance();
        a.id = id;
        a.workerId = workerId;
        a.projectId = projectId;
        a.clockIn = clockIn;
        a.clockOut = clockOut;
        a.status = AttendanceStatus.fromCode(statusCode);
        a.clockInLat = clockInLat;
        a.clockInLng = clockInLng;
        a.clockOutLat = clockOutLat;
        a.clockOutLng = clockOutLng;
        a.locationRisk = locationRisk;
        a.createdAt = createdAt;
        a.updatedAt = updatedAt;
        return a;
    }

    /** 下班打卡，lat/lng 为 null 时跳过位置记录 */
    public void clockOut(Double lat, Double lng, int locationRisk) {
        this.clockOut = LocalDateTime.now();
        this.status = AttendanceStatus.CLOCKED_OUT;
        if (lat != null) this.clockOutLat = BigDecimal.valueOf(lat);
        if (lng != null) this.clockOutLng = BigDecimal.valueOf(lng);
        // 下班打卡风险取最大值
        if (locationRisk > (this.locationRisk != null ? this.locationRisk : 0)) {
            this.locationRisk = locationRisk;
        }
    }

    public boolean isClockedOut() {
        return status == AttendanceStatus.CLOCKED_OUT;
    }
}
