package cn.tx.aiagent.warehouse.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;
import cn.tx.aiagent.warehouse.application.StockApplicationService;
import cn.tx.aiagent.warehouse.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MaterialController {

    private final MaterialRepository materialRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockRecordRepository stockRecordRepository;
    private final StockApplicationService stockApplicationService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public MaterialController(MaterialRepository materialRepository,
                               StockBalanceRepository stockBalanceRepository,
                               StockRecordRepository stockRecordRepository,
                               StockApplicationService stockApplicationService,
                               PurchaseOrderRepository purchaseOrderRepository) {
        this.materialRepository = materialRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockRecordRepository = stockRecordRepository;
        this.stockApplicationService = stockApplicationService;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @GetMapping("/materials")
    public Result<List<Material>> list() {
        return Result.success(materialRepository.findAll());
    }

    @GetMapping("/materials/{id}")
    public Result<Material> getById(@PathVariable Long id) {
        return Result.success(materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND, id)));
    }

    @RequirePermission("material:create")
    @PostMapping("/materials")
    public Result<Material> create(@RequestBody CreateMaterialRequest request) {
        Material m = Material.create(request.getCode(), request.getName(), request.getSpec(), request.getUnit());
        return Result.success(materialRepository.save(m));
    }

    @RequirePermission("material:update")
    @PutMapping("/materials/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateMaterialRequest request) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND, id));
        m = Material.fromPersistence(m.getId(), m.getCode(), request.getName(), request.getSpec(), request.getUnit(),
                m.getIsDeleted(), m.getCreatedAt(), m.getUpdatedAt());
        materialRepository.update(m);
        return Result.success();
    }

    @RequirePermission("material:delete")
    @DeleteMapping("/materials/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialRepository.softDelete(id);
        return Result.success();
    }

    @GetMapping("/materials/{id}/stock")
    public Result<List<StockBalance>> getStockByMaterial(@PathVariable Long id) {
        return Result.success(stockBalanceRepository.findByMaterialId(id));
    }

    @PostMapping("/stock-records")
    public Result<Void> recordStock(@RequestBody CreateStockRecordRequest request) {
        // 根据出入库类型校验不同权限
        String perm = request.getType() == 1 ? "stock:in" : "stock:out";
        if (!LoginContextHolder.getPermissions().contains(perm)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Long userId = LoginContextHolder.getUserId();
        stockApplicationService.recordStock(request.getWarehouseId(), request.getMaterialId(),
                StockType.fromCode(request.getType()), request.getQuantity(),
                request.getPurchaseRequestId(), request.getProjectId(), userId);

        // 入库操作关联采购申请 → PO 流转为已到货，记录入库人
        if (request.getType() == 1 && request.getPurchaseRequestId() != null) {
            purchaseOrderRepository.findByPRId(request.getPurchaseRequestId()).ifPresent(po -> {
                if (po.getStatus() == PurchaseOrderStatus.PURCHASED) {
                    purchaseOrderRepository.updateStatus(po.getId(), PurchaseOrderStatus.ARRIVED);
                    purchaseOrderRepository.updateInboundOperator(po.getId(), userId);
                }
            });
        }
        return Result.success();
    }

    @GetMapping("/stock-records")
    public Result<List<StockRecord>> listStockRecords(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        return Result.success(stockRecordRepository.findAll(warehouseId, materialId, type, offset, size));
    }

    @GetMapping("/warehouses/{id}/stock")
    public Result<List<StockBalance>> getWarehouseStock(@PathVariable Long id) {
        return Result.success(stockBalanceRepository.findByWarehouseId(id));
    }
}
