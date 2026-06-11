package cn.tx.aiagent.attendance.api;

import cn.tx.aiagent.attendance.application.AttendanceApplicationService;
import cn.tx.aiagent.attendance.domain.Attendance;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceApplicationService applicationService;

    public AttendanceController(AttendanceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** 上班打卡（位置参数可选，兼容 Web 端） */
    @RequirePermission("attendance:clock-in")
    @PostMapping("/clock-in")
    public Result<Attendance> clockIn(@RequestBody(required = false) ClockRequest request) {
        ClockRequest req = request != null ? request : new ClockRequest();
        return Result.success(applicationService.clockIn(
                req.getLat(), req.getLng(), req.getAccuracy(),
                req.getVerticalAccuracy(), req.getSpeed(),
                req.getAltitude(), req.getLocationTimestamp()));
    }

    /** 下班打卡（位置参数可选，兼容 Web 端） */
    @RequirePermission("attendance:clock-out")
    @PostMapping("/clock-out")
    public Result<Attendance> clockOut(@RequestBody(required = false) ClockRequest request) {
        ClockRequest req = request != null ? request : new ClockRequest();
        return Result.success(applicationService.clockOut(
                req.getLat(), req.getLng(), req.getAccuracy(),
                req.getVerticalAccuracy(), req.getSpeed(),
                req.getAltitude(), req.getLocationTimestamp()));
    }

    /** 当前用户本月打卡记录 */
    @GetMapping("/my")
    public Result<List<Attendance>> myRecords(@RequestParam(defaultValue = "") String yearMonth) {
        return Result.success(applicationService.myRecords(yearMonth));
    }

    /** 项目月度打卡记录 */
    @GetMapping("/projects/{projectId}")
    public Result<List<Attendance>> projectRecords(@PathVariable Long projectId,
                                                    @RequestParam(defaultValue = "") String yearMonth) {
        return Result.success(applicationService.projectRecords(projectId, yearMonth));
    }

    @Data
    public static class ClockRequest {
        /** 纬度，null 则跳过位置校验（兼容 Web 端） */
        private Double lat;
        /** 经度，null 则跳过位置校验（兼容 Web 端） */
        private Double lng;
        /** 水平定位精度（米），null 视为未知 */
        private Double accuracy;
        /** 垂直精度，-1 表示未知 */
        private double verticalAccuracy = -1;
        /** 速度（m/s），-1 表示未知 */
        private double speed = -1;
        /** 海拔（米），0 表示未知 */
        private double altitude = 0;
        /** 位置获取时间戳（毫秒），0 表示未知 */
        private long locationTimestamp = 0;
    }
}
