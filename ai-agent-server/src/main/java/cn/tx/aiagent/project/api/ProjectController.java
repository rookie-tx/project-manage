package cn.tx.aiagent.project.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.project.application.ProjectApplicationService;
import cn.tx.aiagent.project.application.ProjectDtoMapper;
import cn.tx.aiagent.project.domain.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理 REST 接口，提供项目的增删改查及状态流转。
 * 所有响应使用统一 {@link cn.tx.aiagent.common.api.Result} 包装。
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectApplicationService projectApplicationService;
    private final ProjectDtoMapper dtoMapper;

    public ProjectController(ProjectApplicationService projectApplicationService,
                              ProjectDtoMapper dtoMapper) {
        this.projectApplicationService = projectApplicationService;
        this.dtoMapper = dtoMapper;
    }

    @RequirePermission("project:create")
    @PostMapping
    public Result<ProjectResponse> create(@RequestBody @Validated CreateProjectRequest request) {
        String code = projectApplicationService.generateProjectCode();
        Long currentUserId = projectApplicationService.getCurrentUserId();

        Project project = Project.create(
                code, request.getName(), ProjectType.fromCode(request.getType()),
                request.getLocation(), request.getClientName(), request.getSupervisorUnit(),
                request.getProjectManagerId(), request.getPlannedStartDate(),
                request.getPlannedEndDate(), request.getRemark(), currentUserId);

        Project saved = projectApplicationService.create(project);
        return Result.success(projectApplicationService.toResponse(saved));
    }

    @GetMapping
    public Result<ProjectListResponse> list(ProjectListRequest request) {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria(
                request.getKeyword(), request.getType(), request.getStatus(),
                request.getPage(), request.getSize());
        ProjectListResult result = projectApplicationService.list(criteria);

        List<ProjectResponse> items = result.projects().stream()
                .map(projectApplicationService::toResponse)
                .toList();
        return Result.success(new ProjectListResponse(items, result.total(), result.page(), result.size()));
    }

    @GetMapping("/{id}")
    public Result<ProjectResponse> getById(@PathVariable Long id) {
        Project project = projectApplicationService.getById(id);
        return Result.success(projectApplicationService.toResponse(project));
    }

    @RequirePermission("project:update")
    @PutMapping("/{id}")
    public Result<ProjectResponse> update(@PathVariable Long id,
                                           @RequestBody UpdateProjectRequest request) {
        ProjectUpdateCommand command = dtoMapper.toCommand(request);
        Project updated = projectApplicationService.update(id, command);
        return Result.success(projectApplicationService.toResponse(updated));
    }

    @RequirePermission("project:delete")
    @DeleteMapping("/{id}")
    public Result<Void> softDelete(@PathVariable Long id) {
        projectApplicationService.softDelete(id);
        return Result.success();
    }
}
