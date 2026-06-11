package cn.tx.aiagent.purchaserequest.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchaserequest.api.PurchaseRequestListResponse;
import cn.tx.aiagent.purchaserequest.api.PurchaseRequestResponse;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestDomainService;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
import cn.tx.aiagent.task.application.TaskApplicationService;
import cn.tx.aiagent.task.domain.BusinessType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseRequestApplicationService {

    private final PurchaseRequestDomainService domainService;
    private final PurchaseRequestRepository repository;
    private final PurchaseRequestDtoMapper dtoMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskApplicationService taskApplicationService;

    public PurchaseRequestApplicationService(PurchaseRequestDomainService domainService,
                                              PurchaseRequestRepository repository,
                                              PurchaseRequestDtoMapper dtoMapper,
                                              ProjectRepository projectRepository,
                                              UserRepository userRepository,
                                              TaskApplicationService taskApplicationService) {
        this.domainService = domainService;
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskApplicationService = taskApplicationService;
    }

    public String generateCode() {
        int year = java.time.Year.now().getValue();
        String prefix = "PRQ-" + year + "-";
        String maxCode = repository.findMaxCodeByPrefix(prefix);
        int nextSeq = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            try { nextSeq = Integer.parseInt(maxCode.substring(prefix.length())) + 1; } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%04d", nextSeq);
    }

    public PurchaseRequestResponse create(PurchaseRequest pr) {
        projectRepository.findById(pr.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_PROJECT_NOT_FOUND));
        domainService.create(pr);
        return toResponse(pr);
    }

    public PurchaseRequestListResponse list(Long projectId, Long createdBy, int page, int size) {
        int offset = (page - 1) * size;
        List<PurchaseRequest> items = repository.findByPage(projectId, createdBy, offset, size);
        long total = repository.countByProject(projectId, createdBy);
        List<PurchaseRequestResponse> responses = items.stream()
                .map(this::toResponse)
                .toList();
        return new PurchaseRequestListResponse(responses, total, page, size);
    }

    public PurchaseRequestResponse detail(Long id) {
        PurchaseRequest pr = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));
        return toResponse(pr);
    }

    public PurchaseRequestResponse update(Long id, PurchaseRequest updateData) {
        PurchaseRequest pr = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));

        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }

        pr.update(updateData.getName(), updateData.getSpecification(), updateData.getBrand(),
                updateData.getQuantity(), updateData.getUnit(), updateData.getArrivalDate(),
                updateData.getRemark());

        domainService.update(pr);

        return toResponse(pr);
    }

    public void delete(Long id) {
        PurchaseRequest pr = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));
        if (repository.existsInPurchaseOrder(id)) {
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }
        domainService.softDelete(pr);
    }

    public List<PurchaseRequest> findAvailable() {
        return repository.findAvailable();
    }

    public PurchaseRequestResponse toResponse(PurchaseRequest pr) {
        PurchaseRequestResponse resp = dtoMapper.toResponse(pr);
        projectRepository.findById(pr.getProjectId()).ifPresent(p -> {
            resp.setProjectCode(p.getCode());
            resp.setProjectName(p.getName());
        });
        if (pr.getCreatedBy() != null) {
            userRepository.findById(pr.getCreatedBy())
                    .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                    .ifPresent(resp::setCreatedByName);
        }

        if (pr.getStatus() == PurchaseRequestStatus.APPROVING) {
            String nodeName = taskApplicationService.getCurrentApprovalNode(
                    BusinessType.PURCHASE_REQUEST, pr.getId());
            resp.setCurrentApprovalNode(nodeName);
        }

        return resp;
    }
}
