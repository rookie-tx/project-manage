package cn.tx.aiagent.project.application;

import cn.tx.aiagent.project.api.ProjectResponse;
import cn.tx.aiagent.project.api.UpdateProjectRequest;
import cn.tx.aiagent.project.domain.Project;
import cn.tx.aiagent.project.domain.ProjectUpdateCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper: Domain ↔ API DTO。
 * projectManagerName 由调用方在映射后手动填充。
 */
@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

    @Mapping(target = "type", expression = "java(project.getType() != null ? project.getType().getCode() : null)")
    @Mapping(target = "status", expression = "java(project.getStatus() != null ? project.getStatus().getCode() : null)")
    @Mapping(target = "projectManagerName", ignore = true)
    ProjectResponse toResponse(Project project);

    ProjectUpdateCommand toCommand(UpdateProjectRequest request);
}
