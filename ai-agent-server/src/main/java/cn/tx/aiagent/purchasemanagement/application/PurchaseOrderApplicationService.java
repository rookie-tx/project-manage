package cn.tx.aiagent.purchasemanagement.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.dict.infrastructure.DictMapper;
import cn.tx.aiagent.file.domain.UploadFile;
import java.math.BigDecimal;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchasemanagement.api.PurchaseOrderResponse;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.tx.aiagent.dict.infrastructure.DictPO;

@Service
public class PurchaseOrderApplicationService {

    private final PurchaseOrderRepository repository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final UploadFileRepository uploadFileRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MaterialRepository materialRepository;
    private final DictMapper dictMapper;

    public PurchaseOrderApplicationService(PurchaseOrderRepository repository,
                                            PurchaseRequestRepository purchaseRequestRepository,
                                            UploadFileRepository uploadFileRepository,
                                            UserRepository userRepository,
                                            ProjectRepository projectRepository,
                                            MaterialRepository materialRepository,
                                            DictMapper dictMapper) {
        this.repository = repository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.uploadFileRepository = uploadFileRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.materialRepository = materialRepository;
        this.dictMapper = dictMapper;
    }

    /** 自动创建 PO（审批通过时调用） */
    @Transactional
    public PurchaseOrderResponse createAuto(Long purchaseRequestId, Long createdBy) {
        if (repository.existsByPurchaseRequestId(purchaseRequestId)) {
            return null; // 已存在则跳过
        }
        var pr = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));
        PurchaseOrder po = PurchaseOrder.createAuto(purchaseRequestId, pr.getArrivalDate(), createdBy);
        repository.save(po);
        return toResponse(po);
    }

    /** 手动创建（管理端） */
    @Transactional
    public PurchaseOrderResponse create(Long purchaseRequestId) {
        if (repository.existsByPurchaseRequestId(purchaseRequestId)) {
            throw new BusinessException(ErrorCode.PO_DUPLICATE);
        }
        var pr = purchaseRequestRepository.findById(purchaseRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));
        PurchaseOrder po = PurchaseOrder.createAuto(purchaseRequestId, pr.getArrivalDate(), null);
        repository.save(po);
        return toResponse(po);
    }

    public PurchaseOrderListResponse list(Long projectId, Integer status, String tab, int page, int size) {
        int offset = (page - 1) * size;
        List<PurchaseOrder> items = repository.findByPage(projectId, status, tab, offset, size);
        long total = repository.countByFilter(projectId, status, tab);
        List<PurchaseOrderResponse> responses = items.stream().map(this::toResponse).toList();
        return new PurchaseOrderListResponse(responses, total, page, size);
    }

    /**
     * 待报价采购单列表（含项目名、物料信息）
     */
    public List<Map<String, Object>> listPendingForQuotation() {
        var results = repository.findPendingWithDetails();
        // 预加载单位字典映射 (code -> label)，避免 N+1 查询
        Map<String, String> unitMap = dictMapper.selectByType("UNIT", true).stream()
                .collect(Collectors.toMap(DictPO::getCode, DictPO::getLabel, (a, b) -> b));
        return results.stream().map(r -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("expectedArrivalDate", r.getExpectedArrivalDate());
            m.put("statusCode", r.getStatusCode());
            // 物料名称优先用 pr.name，降级用 material.name
            String matName = r.getMaterialName() != null ? r.getMaterialName()
                    : r.getMaterialName2();
            m.put("materialName", matName);
            m.put("specification", r.getSpecification());
            m.put("brand", r.getBrand());
            // 数量和单位（code 转中文 label）
            String qtyStr = r.getQuantity() != null ? String.valueOf(r.getQuantity()) : "";
            if (r.getUnit() != null && !r.getUnit().isEmpty()) {
                String unitLabel = unitMap.getOrDefault(r.getUnit(), r.getUnit());
                qtyStr += " " + unitLabel;
            }
            m.put("quantityStr", qtyStr.trim().isEmpty() ? "-" : qtyStr.trim());
            m.put("projectName", r.getProjectName());
            return m;
        }).toList();
    }

    public List<PurchaseOrderResponse> listPendingInbound() {
        return repository.findByStatus(PurchaseOrderStatus.PURCHASED.getCode()).stream()
                .map(this::toResponse).toList();
    }

    public PurchaseOrderResponse detail(Long id) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        return toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, String supplierName, String supplierPhone,
                                         BigDecimal unitPrice, BigDecimal totalAmount,
                                         java.time.LocalDate purchaseDate, java.time.LocalDate expectedArrivalDate,
                                         String remark, List<Long> fileIds) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        if (po.getStatus() != PurchaseOrderStatus.PURCHASING) {
            throw new BusinessException(ErrorCode.PO_STATUS_INVALID);
        }
        po.update(supplierName, supplierPhone, unitPrice, totalAmount, purchaseDate, expectedArrivalDate, remark);
        repository.update(po);
        if (fileIds != null) {
            for (Long fid : fileIds) {
                uploadFileRepository.updateBusinessId(fid, "purchase_order", id);
            }
        }
        return toResponse(po);
    }

    public void softDelete(Long id) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        if (po.getStatus() != PurchaseOrderStatus.PURCHASING) {
            throw new BusinessException(ErrorCode.PO_STATUS_INVALID);
        }
        po.markDeleted();
        repository.softDelete(id);
    }

    @Transactional
    public PurchaseOrderResponse changeStatus(Long id, int statusCode) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        PurchaseOrderStatus target = PurchaseOrderStatus.fromCode(statusCode);

        // 采购中→已采购 需要校验供应商信息已填写，并记录采购人
        if (po.getStatus() == PurchaseOrderStatus.PURCHASING && target == PurchaseOrderStatus.PURCHASED) {
            if (!po.isSupplierFilled()) {
                throw new BusinessException(ErrorCode.PO_SUPPLIER_REQUIRED);
            }
            Long userId = LoginContextHolder.getUserId();
            po.setPurchaser(userId);
            repository.updatePurchaser(id, userId);
        }

        try {
            po.changeStatus(target);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PO_STATUS_INVALID);
        }
        repository.updateStatus(id, target);
        return toResponse(po);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        PurchaseOrderResponse resp = new PurchaseOrderResponse();
        resp.setId(po.getId());
        resp.setPurchaseRequestId(po.getPurchaseRequestId());
        resp.setSupplierName(po.getSupplierName());
        resp.setSupplierPhone(po.getSupplierPhone());
        resp.setUnitPrice(po.getUnitPrice());
        resp.setTotalAmount(po.getTotalAmount());
        resp.setPurchaseDate(po.getPurchaseDate());
        resp.setExpectedArrivalDate(po.getExpectedArrivalDate());
        resp.setStatusCode(po.getStatus().getCode());
        resp.setStatusLabel(po.getStatus().getLabel());
        resp.setRemark(po.getRemark());
        resp.setCreatedBy(po.getCreatedBy());
        resp.setCreatedAt(po.getCreatedAt());
        resp.setUpdatedAt(po.getUpdatedAt());

        if (po.getCreatedBy() != null) {
            userRepository.findById(po.getCreatedBy())
                    .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                    .ifPresent(resp::setCreatedByName);
        }

        if (po.getPurchaserId() != null) {
            resp.setPurchaserId(po.getPurchaserId());
            userRepository.findById(po.getPurchaserId())
                    .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                    .ifPresent(resp::setPurchaserName);
        }

        if (po.getInboundOperatorId() != null) {
            resp.setInboundOperatorId(po.getInboundOperatorId());
            userRepository.findById(po.getInboundOperatorId())
                    .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                    .ifPresent(resp::setInboundOperatorName);
        }

        // 1:1 关联单个 PR
        if (po.getPurchaseRequestId() != null) {
            purchaseRequestRepository.findById(po.getPurchaseRequestId()).ifPresent(pr -> {
                PurchaseOrderResponse.PurchaseRequestInfo info = new PurchaseOrderResponse.PurchaseRequestInfo();
                info.setId(pr.getId());
                info.setCode(pr.getCode());
                info.setName(pr.getName());
                info.setSpecification(pr.getSpecification());
                String unitLabel = "";
                if (pr.getUnit() != null) {
                    unitLabel = dictMapper.selectByType("UNIT", true).stream()
                            .filter(d -> d.getCode().equals(pr.getUnit()))
                            .findFirst().map(d -> d.getLabel()).orElse(pr.getUnit());
                }
                info.setQuantityStr(pr.getQuantity() + (unitLabel.isEmpty() ? "" : " " + unitLabel));
                info.setArrivalDate(pr.getArrivalDate() != null ? pr.getArrivalDate().toString() : null);
                info.setMaterialId(pr.getMaterialId());
                projectRepository.findById(pr.getProjectId()).ifPresent(p -> info.setProjectName(p.getName()));
                // 物料信息
                if (pr.getMaterialId() != null) {
                    materialRepository.findById(pr.getMaterialId()).ifPresent(m -> {
                        info.setMaterialName(m.getName());
                        info.setMaterialSpec(m.getSpec());
                    });
                }
                resp.setPurchaseRequests(Collections.singletonList(info));
            });
        } else {
            resp.setPurchaseRequests(Collections.emptyList());
        }

        List<UploadFile> files = uploadFileRepository.findByBusiness("purchase_order", po.getId());
        resp.setFiles(files.stream().map(f -> {
            PurchaseOrderResponse.FileInfo fi = new PurchaseOrderResponse.FileInfo();
            fi.setId(f.getId());
            fi.setOriginalName(f.getOriginalName());
            fi.setContentType(f.getContentType());
            fi.setUrl("/api/files/" + f.getId());
            return fi;
        }).toList());

        return resp;
    }
}
