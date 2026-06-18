package cn.tx.aiagent.quotation.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.dict.infrastructure.DictMapper;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.quotation.domain.Quotation;
import cn.tx.aiagent.quotation.domain.QuotationDomainService;
import cn.tx.aiagent.quotation.domain.QuotationRepository;
import cn.tx.aiagent.supplier.domain.Supplier;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 报价应用服务
 */
@Service
public class QuotationApplicationService {

    private final QuotationDomainService domainService;
    private final QuotationRepository quotationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final QuotationDtoMapper dtoMapper;

    public QuotationApplicationService(QuotationDomainService domainService,
                                        QuotationRepository quotationRepository,
                                        PurchaseOrderRepository purchaseOrderRepository,
                                        SupplierRepository supplierRepository,
                                        UserRepository userRepository,
                                        QuotationDtoMapper dtoMapper) {
        this.domainService = domainService;
        this.quotationRepository = quotationRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    /**
     * 供应商提交/修改报价
     */
    @Transactional
    public QuotationResponse submit(Long purchaseOrderId, Long supplierId,
                                     BigDecimal unitPrice, BigDecimal totalAmount,
                                     LocalDate deliveryDate, String remark, Long createdBy) {
        Quotation q = domainService.submit(purchaseOrderId, supplierId,
                unitPrice, totalAmount, deliveryDate, remark, createdBy);
        return toResponse(q);
    }

    /**
     * 管理员查看某采购单的所有报价
     */
    public List<QuotationResponse> listByOrder(Long purchaseOrderId, int page, int size) {
        int offset = (page - 1) * size;
        return quotationRepository.findByPage(purchaseOrderId, offset, size).stream()
                .map(this::toResponse).toList();
    }

    /**
     * 供应商查看自己的报价列表
     */
    public List<QuotationResponse> listMyQuotations(Long createdBy, int page, int size) {
        int offset = (page - 1) * size;
        return quotationRepository.findByCreatedBy(createdBy, offset, size).stream()
                .map(this::toResponse).toList();
    }

    /**
     * 报价详情
     */
    public QuotationResponse detail(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTATION_NOT_FOUND));
        return toResponse(q);
    }

    /**
     * 报价详情（校验归属权）
     */
    public QuotationResponse detailMyQuotation(Long id, Long userId) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTATION_NOT_FOUND));
        if (!q.getCreatedBy().equals(userId)) {
            throw new BusinessException(ErrorCode.QUOTATION_NOT_OWNER);
        }
        return toResponse(q);
    }

    /**
     * 管理员选用报价
     */
    @Transactional
    public PurchaseOrder selectQuotation(Long quotationId, Long userId) {
        Quotation q = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUOTATION_NOT_FOUND));

        // 获取供应商信息
        Supplier supplier = supplierRepository.findById(q.getSupplierId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND));

        PurchaseOrder po = domainService.selectQuotation(quotationId, userId);

        // 将供应商信息填入采购单（这里需要直接更新采购单的供应商字段）
        // domainService.selectQuotation 已经更新了价格和状态，这里补充供应商名称和电话
        PurchaseOrder poForUpdate = purchaseOrderRepository.findById(po.getId()).orElseThrow();
        poForUpdate.update(supplier.getCompanyName(), supplier.getContactPhone(),
                q.getUnitPrice(), q.getTotalAmount(), null,
                q.getDeliveryDate(), q.getRemark());
        purchaseOrderRepository.update(poForUpdate);

        return po;
    }

    /**
     * 构建响应（含供应商信息和采购单摘要）
     */
    private QuotationResponse toResponse(Quotation q) {
        QuotationResponse resp = dtoMapper.toResponse(q);

        // 填充供应商信息
        if (q.getSupplierId() != null) {
            supplierRepository.findById(q.getSupplierId()).ifPresent(s -> {
                resp.setSupplierCompanyName(s.getCompanyName());
                resp.setSupplierContactPerson(s.getContactPerson());
                resp.setSupplierContactPhone(s.getContactPhone());
                resp.setSupplierName(s.getContactPerson());
            });
        }

        // 填充创建人名称
        if (q.getCreatedBy() != null) {
            userRepository.findById(q.getCreatedBy())
                    .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                    .ifPresent(resp::setCreatedByName);
        }

        // 填充采购单摘要
        if (q.getPurchaseOrderId() != null) {
            purchaseOrderRepository.findById(q.getPurchaseOrderId()).ifPresent(po -> {
                QuotationResponse.PurchaseOrderSummary summary = new QuotationResponse.PurchaseOrderSummary();
                summary.setId(po.getId());
                summary.setExpectedArrivalDate(po.getExpectedArrivalDate());
                resp.setPurchaseOrderInfo(summary);
            });
        }

        return resp;
    }
}
