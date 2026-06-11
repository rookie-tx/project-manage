package cn.tx.aiagent.attendancearea.infrastructure;

import cn.tx.aiagent.attendancearea.domain.AttendanceArea;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceAreaPoMapper {

    default AttendanceArea toDomain(AttendanceAreaPO po) {
        if (po == null) return null;
        return AttendanceArea.fromPersistence(
                po.getId(), po.getProjectId(), po.getName(),
                po.getCenterLat(), po.getCenterLng(), po.getRadius(),
                po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    AttendanceAreaPO toPO(AttendanceArea area);
}
