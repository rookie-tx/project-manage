package cn.tx.aiagent.constructionwaste.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.constructionwaste.domain.ConstructionWaste;
import cn.tx.aiagent.constructionwaste.domain.ConstructionWasteRepository;
import cn.tx.aiagent.file.domain.UploadFile;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import cn.tx.aiagent.project.domain.ProjectRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/construction-wastes")
public class ConstructionWasteController {

    private final ConstructionWasteRepository wasteRepository;
    private final UploadFileRepository uploadFileRepository;
    private final ProjectRepository projectRepository;

    public ConstructionWasteController(ConstructionWasteRepository wasteRepository,
                                        UploadFileRepository uploadFileRepository,
                                        ProjectRepository projectRepository) {
        this.wasteRepository = wasteRepository;
        this.uploadFileRepository = uploadFileRepository;
        this.projectRepository = projectRepository;
    }

    @RequirePermission("waste:read")
    @GetMapping
    public Result<List<ConstructionWasteResponse>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String wasteType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        List<ConstructionWaste> wastes = wasteRepository.search(projectId, wasteType, startDate, endDate, offset, limit);
        List<ConstructionWasteResponse> responses = wastes.stream().map(this::toResponse).toList();
        return Result.success(responses);
    }

    @RequirePermission("waste:read")
    @GetMapping("/{id}")
    public Result<ConstructionWasteResponse> getById(@PathVariable Long id) {
        ConstructionWaste waste = wasteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WASTE_NOT_FOUND, id));
        return Result.success(toResponse(waste));
    }

    @RequirePermission("waste:create")
    @PostMapping
    public Result<ConstructionWasteResponse> create(@Valid @RequestBody CreateConstructionWasteRequest request) {
        Long userId = cn.tx.aiagent.common.config.LoginContextHolder.requireUserId();

        // 校验项目存在
        projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 校验文件存在
        List<UploadFile> files = request.getFileIds().stream()
                .map(fid -> uploadFileRepository.findById(fid)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND)))
                .toList();

        ConstructionWaste waste = ConstructionWaste.create(
                request.getProjectId(), request.getWasteType(), request.getQuantity(),
                request.getUnit(), request.getVehiclePlate(), request.getDriverName(),
                request.getDestination(), request.getRemovalDate(), request.getRemark(),
                userId);

        ConstructionWaste saved = wasteRepository.save(waste);

        // 回填文件关联
        for (UploadFile file : files) {
            uploadFileRepository.updateBusinessId(file.getId(), "construction_waste", saved.getId());
        }

        return Result.success(toResponse(saved));
    }

    @RequirePermission("waste:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ConstructionWaste waste = wasteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WASTE_NOT_FOUND, id));
        wasteRepository.softDelete(id);
        return Result.success();
    }

    private ConstructionWasteResponse toResponse(ConstructionWaste waste) {
        ConstructionWasteResponse resp = new ConstructionWasteResponse();
        resp.setId(waste.getId());
        resp.setProjectId(waste.getProjectId());
        resp.setWasteType(waste.getWasteType());
        resp.setQuantity(waste.getQuantity());
        resp.setUnit(waste.getUnit());
        resp.setVehiclePlate(waste.getVehiclePlate());
        resp.setDriverName(waste.getDriverName());
        resp.setDestination(waste.getDestination());
        resp.setRemovalDate(waste.getRemovalDate());
        resp.setRemark(waste.getRemark());
        resp.setOperatorId(waste.getOperatorId());
        resp.setCreatedAt(waste.getCreatedAt());
        resp.setUpdatedAt(waste.getUpdatedAt());

        // 填充项目名称
        projectRepository.findById(waste.getProjectId())
                .ifPresent(p -> resp.setProjectName(p.getName()));

        // 填充关联文件
        List<UploadFile> files = uploadFileRepository.findByBusiness("construction_waste", waste.getId());
        resp.setFiles(files.stream().map(ConstructionWasteResponse.FileInfo::from).toList());

        return resp;
    }
}
