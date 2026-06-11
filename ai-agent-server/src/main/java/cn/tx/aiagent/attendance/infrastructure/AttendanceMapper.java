package cn.tx.aiagent.attendance.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttendanceMapper {
    AttendancePO selectTodayByWorker(@Param("workerId") Long workerId);
    void insert(AttendancePO po);
    void update(AttendancePO po);
    List<AttendancePO> selectByWorkerMonth(@Param("workerId") Long workerId, @Param("yearMonth") String yearMonth);
    List<AttendancePO> selectByProjectMonth(@Param("projectId") Long projectId, @Param("yearMonth") String yearMonth);
}
