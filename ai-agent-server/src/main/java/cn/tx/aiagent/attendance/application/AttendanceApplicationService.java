package cn.tx.aiagent.attendance.application;

import cn.tx.aiagent.attendance.domain.Attendance;
import cn.tx.aiagent.attendance.domain.AttendanceRepository;
import cn.tx.aiagent.attendancearea.domain.AttendanceArea;
import cn.tx.aiagent.attendancearea.domain.AttendanceAreaRepository;
import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.util.LocationUtils;
import cn.tx.aiagent.tempworker.domain.TempWorker;
import cn.tx.aiagent.tempworker.domain.TempWorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 打卡应用服务，编排打卡流程（位置校验 + 风险评估 + 持久化） */
@Service
public class AttendanceApplicationService {

    private final AttendanceRepository attendanceRepository;
    private final TempWorkerRepository tempWorkerRepository;
    private final AttendanceAreaRepository attendanceAreaRepository;

    public AttendanceApplicationService(AttendanceRepository attendanceRepository,
                                         TempWorkerRepository tempWorkerRepository,
                                         AttendanceAreaRepository attendanceAreaRepository) {
        this.attendanceRepository = attendanceRepository;
        this.tempWorkerRepository = tempWorkerRepository;
        this.attendanceAreaRepository = attendanceAreaRepository;
    }

    /** 上班打卡（位置参数可选，为 null 则跳过位置校验） */
    @Transactional(rollbackFor = Exception.class)
    public Attendance clockIn(Double lat, Double lng, Double accuracy,
                               double verticalAccuracy, double speed,
                               double altitude, long locationTimestamp) {
        TempWorker worker = findCurrentWorker();
        if (attendanceRepository.findTodayByWorker(worker.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_CLOCKED_IN);
        }
        int risk = 0;
        if (lat != null && lng != null) {
            validateLocation(worker.getProjectId(), lat, lng);
            risk = LocationUtils.assessLocationRisk(
                    accuracy != null ? accuracy : -1, verticalAccuracy, speed,
                    altitude, locationTimestamp, System.currentTimeMillis());
        }
        Attendance a = Attendance.create(worker.getId(), worker.getProjectId(), lat, lng, risk);
        return attendanceRepository.save(a);
    }

    /** 下班打卡（位置参数可选，为 null 则跳过位置校验） */
    @Transactional(rollbackFor = Exception.class)
    public Attendance clockOut(Double lat, Double lng, Double accuracy,
                                double verticalAccuracy, double speed,
                                double altitude, long locationTimestamp) {
        TempWorker worker = findCurrentWorker();
        Attendance today = attendanceRepository.findTodayByWorker(worker.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CLOCKED_IN));
        if (today.isClockedOut()) {
            throw new BusinessException(ErrorCode.ALREADY_CLOCKED_OUT);
        }
        int risk = 0;
        if (lat != null && lng != null) {
            validateLocation(worker.getProjectId(), lat, lng);
            risk = LocationUtils.assessLocationRisk(
                    accuracy != null ? accuracy : -1, verticalAccuracy, speed,
                    altitude, locationTimestamp, System.currentTimeMillis());
        }
        today.clockOut(lat, lng, risk);
        attendanceRepository.update(today);
        return today;
    }

    /** 查询当前用户本月打卡记录 */
    public List<Attendance> myRecords(String yearMonth) {
        TempWorker worker = findCurrentWorker();
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = java.time.YearMonth.now().toString();
        }
        return attendanceRepository.findByWorkerMonth(worker.getId(), yearMonth);
    }

    /** 查询项目本月打卡记录 */
    public List<Attendance> projectRecords(Long projectId, String yearMonth) {
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = java.time.YearMonth.now().toString();
        }
        return attendanceRepository.findByProjectMonth(projectId, yearMonth);
    }

    /** 校验用户位置是否在项目任一打卡区域内 */
    private void validateLocation(Long projectId, double userLat, double userLng) {
        List<AttendanceArea> areas = attendanceAreaRepository.findByProjectId(projectId);
        if (areas.isEmpty()) {
            return; // 项目未配置打卡区域时放行
        }
        for (AttendanceArea area : areas) {
            double distance = LocationUtils.haversine(
                    userLat, userLng,
                    area.getCenterLat().doubleValue(),
                    area.getCenterLng().doubleValue());
            if (distance <= area.getRadius()) {
                return; // 在某个区域内
            }
        }
        throw new BusinessException(ErrorCode.OUT_OF_CHECKIN_RANGE);
    }

    private TempWorker findCurrentWorker() {
        Long userId = LoginContextHolder.getUserId();
        return tempWorkerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMP_WORKER_NOT_FOUND, userId));
    }
}
