package cn.tx.aiagent.purchasemanagement.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.file.domain.UploadFile;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import cn.tx.aiagent.purchasemanagement.application.PurchaseOrderApplicationService;
import cn.tx.aiagent.purchasemanagement.application.PurchaseOrderListResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderApplicationService applicationService;
    private final UploadFileRepository uploadFileRepository;

    public PurchaseOrderController(PurchaseOrderApplicationService applicationService,
                                    UploadFileRepository uploadFileRepository) {
        this.applicationService = applicationService;
        this.uploadFileRepository = uploadFileRepository;
    }

    @RequirePermission("purchase:create")
    @PostMapping
    public Result<PurchaseOrderResponse> create(@RequestBody @Valid CreatePurchaseOrderRequest request) {
        return Result.success(applicationService.create(request.getPurchaseRequestId()));
    }

    @GetMapping
    public Result<PurchaseOrderListResponse> list(@RequestParam(required = false) Long projectId,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String tab,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.success(applicationService.list(projectId, status, tab, page, size));
    }

    @GetMapping("/pending-inbound")
    public Result<List<PurchaseOrderResponse>> pendingInbound() {
        return Result.success(applicationService.listPendingInbound());
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrderResponse> detail(@PathVariable Long id) {
        return Result.success(applicationService.detail(id));
    }

    @RequirePermission("purchase:update")
    @PutMapping("/{id}")
    public Result<PurchaseOrderResponse> update(@PathVariable Long id,
                                                  @RequestBody UpdatePurchaseOrderRequest request) {
        return Result.success(applicationService.update(id,
                request.getSupplierName(), request.getSupplierPhone(),
                request.getUnitPrice(), request.getTotalAmount(),
                request.getPurchaseDate(), request.getExpectedArrivalDate(),
                request.getRemark(), request.getFileIds()));
    }

    @RequirePermission("purchase:update")
    @PutMapping("/{id}/status")
    public Result<PurchaseOrderResponse> changeStatus(@PathVariable Long id,
                                                        @RequestBody @Validated ChangeStatusRequest request) {
        return Result.success(applicationService.changeStatus(id, request.getStatus()));
    }

    @RequirePermission("purchase:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.softDelete(id);
        return Result.success();
    }
}
