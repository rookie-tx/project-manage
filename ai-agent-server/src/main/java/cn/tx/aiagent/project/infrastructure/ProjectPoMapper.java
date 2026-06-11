package cn.tx.aiagent.project.infrastructure;

import cn.tx.aiagent.project.domain.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper: ProjectPO ↔ Project。
 * toDomain 委托给 {@link Project#fromPersistence} 处理枚举还原，
 * toPO 由 MapStruct 自动生成，枚举→code 通过表达式转换。
 */
@Mapper(componentModel = "spring")
public interface ProjectPoMapper {

    default Project toDomain(ProjectPO po) {
        if (po == null) {
            return null;
        }
        return Project.fromPersistence(
                po.getId(), po.getCode(), po.getName(), po.getType(),
                po.getLocation(), po.getClientName(), po.getSupervisorUnit(),
                po.getProjectManagerId(), po.getPlannedStartDate(),
                po.getPlannedEndDate(), po.getActualStartDate(),
                po.getActualEndDate(), po.getStatus(), po.getRemark(),
                po.getIsDeleted(), po.getCreatedBy(),
                po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    @Mapping(target = "type", expression = "java(project.getType() != null ? project.getType().getCode() : null)")
    @Mapping(target = "status", expression = "java(project.getStatus() != null ? project.getStatus().getCode() : null)")
    ProjectPO toPO(Project project);
}
