package cn.tx.aiagent.attendance.domain;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    Optional<Attendance> findTodayByWorker(Long workerId);
    Attendance save(Attendance attendance);
    void update(Attendance attendance);
    List<Attendance> findByWorkerMonth(Long workerId, String yearMonth);
    List<Attendance> findByProjectMonth(Long projectId, String yearMonth);
}
