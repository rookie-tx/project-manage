package cn.tx.aiagent.attendancearea.domain;

import java.util.List;
import java.util.Optional;

/** 打卡区域仓储接口 */
public interface AttendanceAreaRepository {
    /** 查询项目下所有未删除的打卡区域 */
    List<AttendanceArea> findByProjectId(Long projectId);
    /** 按 ID 查询单个区域 */
    Optional<AttendanceArea> findById(Long id);
    /** 新增打卡区域 */
    AttendanceArea save(AttendanceArea area);
    /** 更新打卡区域 */
    void update(AttendanceArea area);
    /** 软删除打卡区域 */
    void softDelete(Long id);
}
