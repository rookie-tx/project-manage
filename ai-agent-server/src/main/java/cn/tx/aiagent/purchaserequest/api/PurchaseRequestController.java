package cn.tx.aiagent.purchaserequest.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.purchaserequest.application.PurchaseRequestApplicationService;
import cn.tx.aiagent.purchaserequest.application.PurchaseRequestDtoMapper;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
public class PurchaseRequestController {

    private final PurchaseRequestApplicationService applicationService;
    private final PurchaseRequestDtoMapper dtoMapper;

    public PurchaseRequestController(PurchaseRequestApplicationService applicationService,
                                      PurchaseRequestDtoMapper dtoMapper) {
        this.applicationService = applicationService;
        this.dtoMapper = dtoMapper;
    }

    @RequirePermission("pr:create")
    @PostMapping
    public Result<PurchaseRequestResponse> create(@RequestBody @Validated CreatePurchaseRequestRequest request) {
        PurchaseRequest pr = dtoMapper.toDomain(request);
        pr.setCode(applicationService.generateCode());
        pr.setCreatedBy(LoginContextHolder.requireUserId());
        return Result.success(applicationService.create(pr));
    }

    @GetMapping("/available")
    public Result<List<PurchaseRequestResponse>> available() {
        return Result.success(applicationService.findAvailable().stream()
                .map(applicationService::toResponse).toList());
    }

    @GetMapping
    public Result<PurchaseRequestListResponse> list(@RequestParam(required = false) Long projectId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Long createdBy = null;
        LoginContext ctx = LoginContextHolder.get();
        if (ctx == null || ctx.roles() == null || !ctx.roles().contains("admin")) {
            createdBy = ctx != null ? ctx.userId() : null;
        }
        return Result.success(applicationService.list(projectId, createdBy, page, size));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequestResponse> detail(@PathVariable Long id) {
        return Result.success(applicationService.detail(id));
    }

    @RequirePermission("pr:approve")
    @PutMapping("/{id}")
    public Result<PurchaseRequestResponse> update(@PathVariable Long id,
                                                   @RequestBody @Validated UpdatePurchaseRequestRequest request) {
        return Result.success(applicationService.update(id, dtoMapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.success();
    }
}
