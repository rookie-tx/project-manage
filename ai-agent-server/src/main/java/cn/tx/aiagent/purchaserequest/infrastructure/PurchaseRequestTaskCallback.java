package cn.tx.aiagent.purchaserequest.infrastructure;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
import cn.tx.aiagent.purchasemanagement.application.PurchaseOrderApplicationService;
import cn.tx.aiagent.task.domain.BusinessInfo;
import cn.tx.aiagent.task.domain.BusinessTaskCallback;
import cn.tx.aiagent.task.domain.BusinessType;
import cn.tx.aiagent.task.domain.TaskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PurchaseRequestTaskCallback implements BusinessTaskCallback {

    private final PurchaseRequestRepository prRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderApplicationService poApplicationService;

    public PurchaseRequestTaskCallback(PurchaseRequestRepository prRepository,
                                       UserRepository userRepository,
                                       PurchaseOrderApplicationService poApplicationService) {
        this.prRepository = prRepository;
        this.userRepository = userRepository;
        this.poApplicationService = poApplicationService;
    }

    @Override
    public BusinessType getType() {
        return BusinessType.PURCHASE_REQUEST;
    }

    @Override
    public BusinessInfo getBusinessInfo(Long businessId) {
        PurchaseRequest pr = prRepository.findById(businessId).orElse(null);
        if (pr == null) {
            return new BusinessInfo("", "");
        }
        return new BusinessInfo(getType().getName(), pr.getName());
    }

    @Override
    public void validateBeforeStart(Long businessId, Map<String, Object> variables) {
        PurchaseRequest pr = prRepository.findById(businessId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));

        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.PR_STATUS_TRANSITION_INVALID);
        }

        // 仅校验项目经理
        Object pmValue = variables.get("pmUserId");
        if (pmValue instanceof Number n) {
            userRepository.findById(n.longValue())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID));
        } else if (pmValue instanceof String s) {
            userRepository.findById(Long.parseLong(s))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID));
        } else {
            throw new BusinessException(ErrorCode.PARAM_MISSING);
        }
    }

    @Override
    public void onProcessStarted(Long businessId) {
        prRepository.updateStatus(businessId, PurchaseRequestStatus.APPROVING);
    }

    @Override
    public void onTaskCompleted(Long businessId, TaskResult result) {
        if (result instanceof TaskResult.Approved) {
            prRepository.updateStatus(businessId, PurchaseRequestStatus.APPROVED);
            // 审批通过 → 自动创建 PO
            PurchaseRequest pr = prRepository.findById(businessId).orElse(null);
            if (pr != null) {
                poApplicationService.createAuto(businessId, pr.getCreatedBy());
            }
        } else {
            prRepository.updateStatus(businessId, PurchaseRequestStatus.REJECTED);
        }
    }
}
