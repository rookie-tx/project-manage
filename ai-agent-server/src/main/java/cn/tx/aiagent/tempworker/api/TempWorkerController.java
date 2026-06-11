package cn.tx.aiagent.tempworker.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.tempworker.domain.TempWorker;
import cn.tx.aiagent.tempworker.domain.TempWorkerDomainService;
import cn.tx.aiagent.tempworker.domain.TempWorkerRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TempWorkerController {

    private final TempWorkerRepository tempWorkerRepository;
    private final TempWorkerDomainService domainService;

    public TempWorkerController(TempWorkerRepository tempWorkerRepository, TempWorkerDomainService domainService) {
        this.tempWorkerRepository = tempWorkerRepository; this.domainService = domainService;
    }

    @GetMapping("/projects/{projectId}/temp-workers")
    public Result<List<TempWorker>> listByProject(@PathVariable Long projectId) {
        return Result.success(tempWorkerRepository.findByProjectId(projectId));
    }

    @RequirePermission("user:create")
    @PostMapping("/temp-workers")
    public Result<Void> create(@Valid @RequestBody CreateTempWorkerRequest request) {
        domainService.create(request.getPhone(), request.getName(), request.getProjectId(), request.getWorkerType(), request.getEntryDate());
        return Result.success();
    }

    @RequirePermission("user:update")
    @PutMapping("/temp-workers/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateTempWorkerRequest request) {
        TempWorker w = tempWorkerRepository.findById(id)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.TEMP_WORKER_NOT_FOUND, id));
        if (request.getExitDate() != null) {
            domainService.leave(id, request.getExitDate());
        } else {
            w = TempWorker.fromPersistence(w.getId(), w.getUserId(), w.getUsername(), w.getName(), w.getPhone(), w.getProjectId(),
                    request.getWorkerType() != null ? request.getWorkerType() : w.getWorkerType(),
                    request.getEntryDate() != null ? request.getEntryDate() : w.getEntryDate(),
                    w.getExitDate(), w.getStatus().getCode(), w.getIsDeleted(), w.getCreatedAt(), w.getUpdatedAt());
            tempWorkerRepository.update(w);
        }
        return Result.success();
    }

    @RequirePermission("user:delete")
    @DeleteMapping("/temp-workers/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tempWorkerRepository.softDelete(id);
        return Result.success();
    }
}
