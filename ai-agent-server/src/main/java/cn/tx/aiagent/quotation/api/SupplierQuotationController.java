package cn.tx.aiagent.quotation.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.purchasemanagement.api.PurchaseOrderResponse;
import cn.tx.aiagent.purchasemanagement.application.PurchaseOrderApplicationService;
import cn.tx.aiagent.quotation.application.QuotationApplicationService;
import cn.tx.aiagent.quotation.application.QuotationResponse;
import cn.tx.aiagent.quotation.application.SubmitQuotationRequest;
import cn.tx.aiagent.supplier.domain.Supplier;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 供应商端报价控制器（小程序接口）
 */
@RestController
@RequestMapping("/api/supplier")
public class SupplierQuotationController {

    private final QuotationApplicationService quotationApplicationService;
    private final PurchaseOrderApplicationService purchaseOrderApplicationService;
    private final SupplierRepository supplierRepository;

    public SupplierQuotationController(QuotationApplicationService quotationApplicationService,
                                        PurchaseOrderApplicationService purchaseOrderApplicationService,
                                        SupplierRepository supplierRepository) {
        this.quotationApplicationService = quotationApplicationService;
        this.purchaseOrderApplicationService = purchaseOrderApplicationService;
        this.supplierRepository = supplierRepository;
    }

    /**
     * 获取当前供应商
     */
    private Supplier getCurrentSupplier() {
        LoginContext ctx = LoginContextHolder.get();
        return supplierRepository.findByUserId(ctx.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND));
    }

    /**
     * 待报价采购单列表（含项目名、物料信息）
     */
    @RequirePermission("supplier:profile")
    @GetMapping("/pending-orders")
    public Result<Map<String, Object>> listPendingOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var allItems = purchaseOrderApplicationService.listPendingForQuotation();
        int offset = (page - 1) * size;
        long total = allItems.size();
        int fromIndex = Math.min(offset, allItems.size());
        int toIndex = Math.min(offset + size, allItems.size());
        List<Map<String, Object>> paged = allItems.subList(fromIndex, toIndex);

        return Result.success(Map.of(
                "records", paged,
                "total", total,
                "page", page,
                "size", size
        ));
    }

    /**
     * 待报价采购单详情（含是否已报价）
     */
    @RequirePermission("supplier:profile")
    @GetMapping("/pending-orders/{id}")
    public Result<Map<String, Object>> getPendingOrderDetail(@PathVariable Long id) {
        PurchaseOrderResponse detail = purchaseOrderApplicationService.detail(id);
        Supplier supplier = getCurrentSupplier();
        boolean hasQuoted = quotationApplicationService.listMyQuotations(
                LoginContextHolder.getUserId(), 1, 100).stream()
                .anyMatch(q -> q.getPurchaseOrderId().equals(id));

        return Result.success(Map.of(
                "order", detail,
                "hasQuoted", hasQuoted
        ));
    }

    /**
     * 提交/修改报价
     */
    @RequirePermission("supplier:profile")
    @PostMapping("/quotations")
    public Result<QuotationResponse> submitQuotation(@Valid @RequestBody SubmitQuotationRequest request) {
        Supplier supplier = getCurrentSupplier();
        Long userId = LoginContextHolder.getUserId();
        QuotationResponse resp = quotationApplicationService.submit(
                request.getPurchaseOrderId(),
                supplier.getId(),
                request.getUnitPrice(),
                request.getTotalAmount(),
                request.getDeliveryDate(),
                request.getRemark(),
                userId);
        return Result.success(resp);
    }

    /**
     * 我的报价列表
     */
    @RequirePermission("supplier:profile")
    @GetMapping("/quotations")
    public Result<Map<String, Object>> listMyQuotations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = LoginContextHolder.getUserId();
        List<QuotationResponse> records = quotationApplicationService.listMyQuotations(userId, page, size);
        return Result.success(Map.of(
                "records", records,
                "page", page,
                "size", size
        ));
    }

    /**
     * 我的报价详情
     */
    @RequirePermission("supplier:profile")
    @GetMapping("/quotations/{id}")
    public Result<QuotationResponse> getMyQuotationDetail(@PathVariable Long id) {
        Long userId = LoginContextHolder.getUserId();
        QuotationResponse resp = quotationApplicationService.detailMyQuotation(id, userId);
        return Result.success(resp);
    }
}
