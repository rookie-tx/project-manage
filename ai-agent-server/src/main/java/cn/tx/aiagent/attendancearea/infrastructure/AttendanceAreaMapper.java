package cn.tx.aiagent.attendancearea.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttendanceAreaMapper {
    List<AttendanceAreaPO> selectByProjectId(@Param("projectId") Long projectId);
    AttendanceAreaPO selectById(@Param("id") Long id);
    void insert(AttendanceAreaPO po);
    void update(AttendanceAreaPO po);
    void softDelete(@Param("id") Long id);
}
