package cn.tx.aiagent.warehouse.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.warehouse.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    public Result<List<Warehouse>> list() {
        return Result.success(warehouseRepository.findAll());
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.success(warehouseRepository.findById(id)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.WAREHOUSE_NOT_FOUND, id)));
    }

    @RequirePermission("warehouse:create")
    @PostMapping
    public Result<Warehouse> create(@RequestBody CreateWarehouseRequest request) {
        Warehouse w = Warehouse.create(request.getCode(), request.getName(), request.getLocation());
        return Result.success(warehouseRepository.save(w));
    }

    @RequirePermission("warehouse:update")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateWarehouseRequest request) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.WAREHOUSE_NOT_FOUND, id));
        w = Warehouse.fromPersistence(w.getId(), w.getCode(), request.getName(), request.getLocation(),
                request.getStatus() != null ? request.getStatus() : w.getStatus().getCode(),
                w.getIsDeleted(), w.getCreatedAt(), w.getUpdatedAt());
        warehouseRepository.update(w);
        return Result.success();
    }

    @RequirePermission("warehouse:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseRepository.softDelete(id);
        return Result.success();
    }
}
