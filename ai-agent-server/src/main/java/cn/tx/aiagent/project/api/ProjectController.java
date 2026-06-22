package cn.tx.aiagent.project.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.project.application.ProjectApplicationService;
import cn.tx.aiagent.project.application.ProjectDtoMapper;
import cn.tx.aiagent.project.domain.*;
import cn.tx.aiagent.project.infrastructure.MaterialPurchaseStatsResult;
import cn.tx.aiagent.project.infrastructure.ProjectMapper;
import cn.tx.aiagent.dict.infrastructure.DictMapper;
import cn.tx.aiagent.dict.infrastructure.DictPO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目管理 REST 接口，提供项目的增删改查及状态流转。
 * 所有响应使用统一 {@link cn.tx.aiagent.common.api.Result} 包装。
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectApplicationService projectApplicationService;
    private final ProjectDtoMapper dtoMapper;
    private final ProjectMapper projectMapper;
    private final DictMapper dictMapper;

    public ProjectController(ProjectApplicationService projectApplicationService,
                              ProjectDtoMapper dtoMapper,
                              ProjectMapper projectMapper,
                              DictMapper dictMapper) {
        this.projectApplicationService = projectApplicationService;
        this.dtoMapper = dtoMapper;
        this.projectMapper = projectMapper;
        this.dictMapper = dictMapper;
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

    /** 查询指定项目下已完成采购的物料汇总统计 */
    @GetMapping("/{id}/purchase-statistics")
    public Result<List<MaterialPurchaseStatsResponse>> getMaterialPurchaseStats(@PathVariable Long id) {
        List<MaterialPurchaseStatsResult> results = projectMapper.selectMaterialPurchaseStats(id);

        // 预加载单位字典 code → label 映射
        Map<String, String> unitMap = dictMapper.selectByType("UNIT", true).stream()
                .collect(Collectors.toMap(DictPO::getCode, DictPO::getLabel, (a, b) -> b));

        List<MaterialPurchaseStatsResponse> list = results.stream().map(r -> {
            MaterialPurchaseStatsResponse resp = new MaterialPurchaseStatsResponse();
            resp.setMaterialId(r.getMaterialId());
            resp.setMaterialName(r.getMaterialName());
            resp.setMaterialSpec(r.getMaterialSpec());
            // 单位 code 转中文 label，查不到则保留原 code
            String unitLabel = unitMap.getOrDefault(r.getMaterialUnit(), r.getMaterialUnit());
            resp.setMaterialUnit(unitLabel);
            resp.setTotalQuantity(r.getTotalQuantity());
            resp.setTotalAmount(r.getTotalAmount());
            return resp;
        }).toList();
        return Result.success(list);
    }
}
