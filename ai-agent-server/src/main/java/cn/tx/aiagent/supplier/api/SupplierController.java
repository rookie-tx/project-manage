package cn.tx.aiagent.supplier.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.supplier.application.SupplierApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商管理控制器（Web 管理端 + 小程序个人资料）。
 */
@RestController
@RequestMapping("/api")
public class SupplierController {

    private final SupplierApplicationService applicationService;

    public SupplierController(SupplierApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ========== 管理端 CRUD ==========

    @RequirePermission("supplier:read")
    @GetMapping("/suppliers")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        List<SupplierResponse> records = applicationService.list(keyword, status, page, size);
        long total = applicationService.count(keyword, status);
        return Result.success(Map.of(
                "records", records,
                "total", total,
                "page", page,
                "size", size
        ));
    }

    @RequirePermission("supplier:read")
    @GetMapping("/suppliers/{id}")
    public Result<SupplierResponse> getById(@PathVariable Long id) {
        return Result.success(applicationService.getById(id));
    }

    @RequirePermission("supplier:create")
    @PostMapping("/suppliers")
    public Result<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        return Result.success(applicationService.create(
                request.getUsername(), request.getPassword(),
                request.getCompanyName(), request.getLicenseNo(),
                request.getContactPerson(), request.getContactPhone(),
                request.getAddress()));
    }

    @RequirePermission("supplier:update")
    @PutMapping("/suppliers/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateSupplierRequest request) {
        applicationService.update(id, request.getCompanyName(), request.getLicenseNo(),
                request.getContactPerson(), request.getContactPhone(),
                request.getAddress(), request.getRemark(), request.getStatus());
        return Result.success();
    }

    @RequirePermission("supplier:delete")
    @DeleteMapping("/suppliers/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.softDelete(id);
        return Result.success();
    }

    // ========== 供应商个人资料（小程序） ==========

    @RequirePermission("supplier:profile")
    @GetMapping("/supplier/my-profile")
    public Result<SupplierResponse> getMyProfile() {
        LoginContext ctx = LoginContextHolder.get();
        return Result.success(applicationService.getMyProfile(ctx.userId()));
    }

    @RequirePermission("supplier:profile")
    @PutMapping("/supplier/my-profile")
    public Result<Void> updateMyProfile(@RequestBody UpdateSupplierProfileRequest request) {
        LoginContext ctx = LoginContextHolder.get();
        applicationService.updateMyProfile(ctx.userId(),
                request.getContactPerson(), request.getContactPhone(), request.getAddress());
        return Result.success();
    }
}
