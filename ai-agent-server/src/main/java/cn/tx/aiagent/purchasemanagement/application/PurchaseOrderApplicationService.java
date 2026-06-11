package cn.tx.aiagent.purchasemanagement.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.file.domain.UploadFile;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import cn.tx.aiagent.purchasemanagement.api.PurchaseOrderResponse;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderStatus;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseOrderApplicationService {

    private final PurchaseOrderRepository repository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final UploadFileRepository uploadFileRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public PurchaseOrderApplicationService(PurchaseOrderRepository repository,
                                            PurchaseRequestRepository purchaseRequestRepository,
                                            UploadFileRepository uploadFileRepository,
                                            UserRepository userRepository,
                                            ProjectRepository projectRepository) {
        this.repository = repository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.uploadFileRepository = uploadFileRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrder po, List<Long> purchaseRequestIds, List<Long> fileIds) {
        repository.save(po);
        for (Long prId : purchaseRequestIds) {
            repository.insertItem(po.getId(), prId);
        }
        for (Long fid : fileIds) {
            uploadFileRepository.updateBusinessId(fid, "purchase_order", po.getId());
        }
        return toResponse(po);
    }

    public PurchaseOrderListResponse list(Long projectId, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        List<PurchaseOrder> items = repository.findByPage(projectId, status, offset, size);
        long total = repository.countByFilter(projectId, status);
        List<PurchaseOrderResponse> responses = items.stream().map(this::toResponse).toList();
        return new PurchaseOrderListResponse(responses, total, page, size);
    }

    public PurchaseOrderResponse detail(Long id) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        return toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrder updateData, List<Long> purchaseRequestIds, List<Long> fileIds) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        if (po.getStatus() != PurchaseOrderStatus.PURCHASING) {
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }
        po.update(updateData.getSupplierName(), updateData.getSupplierPhone(),
                updateData.getUnitPrice(), updateData.getTotalAmount(),
                updateData.getPurchaseDate(), updateData.getExpectedArrivalDate(),
                updateData.getRemark());
        repository.update(po);
        if (purchaseRequestIds != null && !purchaseRequestIds.isEmpty()) {
            repository.deleteItemsByOrderId(id);
            for (Long prId : purchaseRequestIds) {
                repository.insertItem(id, prId);
            }
        }
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
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }
        po.markDeleted();
        repository.softDelete(id);
    }

    public PurchaseOrderResponse changeStatus(Long id, int statusCode) {
        PurchaseOrder po = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ERROR, "采购记录不存在"));
        PurchaseOrderStatus target = PurchaseOrderStatus.fromCode(statusCode);
        try {
            po.changeStatus(target);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }
        repository.updateStatus(id, target);
        return toResponse(po);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        PurchaseOrderResponse resp = new PurchaseOrderResponse();
        resp.setId(po.getId());
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
            userRepository.findById(po.getCreatedBy()).map(u -> u.getName() != null ? u.getName() : u.getUsername()).ifPresent(resp::setCreatedByName);
        }
        List<Long> prIds = repository.findRequestIdsByOrderId(po.getId());
        List<PurchaseOrderResponse.PurchaseRequestInfo> prInfos = prIds.stream().map(prId -> {
            PurchaseOrderResponse.PurchaseRequestInfo info = new PurchaseOrderResponse.PurchaseRequestInfo();
            info.setId(prId);
            purchaseRequestRepository.findById(prId).ifPresent(pr -> {
                info.setCode(pr.getCode());
                info.setName(pr.getName());
                info.setSpecification(pr.getSpecification());
                info.setQuantityStr(pr.getQuantity() + (pr.getUnit() != null ? pr.getUnit().getLabel() : ""));
                info.setArrivalDate(pr.getArrivalDate() != null ? pr.getArrivalDate().toString() : null);
                projectRepository.findById(pr.getProjectId()).ifPresent(p -> info.setProjectName(p.getName()));
            });
            return info;
        }).toList();
        resp.setPurchaseRequests(prInfos);

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
