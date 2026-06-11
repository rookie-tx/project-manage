package cn.tx.aiagent.attendance.infrastructure;

import cn.tx.aiagent.attendance.domain.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendancePoMapper {
    default Attendance toDomain(AttendancePO po) {
        if (po == null) return null;
        return Attendance.fromPersistence(po.getId(), po.getWorkerId(), po.getProjectId(),
                po.getClockIn(), po.getClockOut(), po.getStatus(),
                po.getClockInLat(), po.getClockInLng(),
                po.getClockOutLat(), po.getClockOutLng(),
                po.getLocationRisk() != null ? po.getLocationRisk() : 0,
                po.getCreatedAt(), po.getUpdatedAt());
    }
    @Mapping(target = "status", expression = "java(a.getStatus() != null ? a.getStatus().getCode() : null)")
    AttendancePO toPO(Attendance a);
}
