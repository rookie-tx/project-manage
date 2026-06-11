package cn.tx.aiagent.purchaserequest.infrastructure;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
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

    public PurchaseRequestTaskCallback(PurchaseRequestRepository prRepository,
                                       UserRepository userRepository) {
        this.prRepository = prRepository;
        this.userRepository = userRepository;
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

        validateApproverExists(variables, "deptManagerId");
        validateApproverExists(variables, "financeManagerId");
        validateApproverExists(variables, "generalManagerId");
    }

    @Override
    public void onProcessStarted(Long businessId) {
        prRepository.updateStatus(businessId, PurchaseRequestStatus.APPROVING);
    }

    @Override
    public void onTaskCompleted(Long businessId, TaskResult result) {
        PurchaseRequestStatus newStatus = switch (result) {
            case TaskResult.Approved __ -> PurchaseRequestStatus.APPROVED;
            case TaskResult.Rejected __ -> PurchaseRequestStatus.REJECTED;
        };
        prRepository.updateStatus(businessId, newStatus);
    }

    private void validateApproverExists(Map<String, Object> variables, String key) {
        Object value = variables.get(key);
        if (value instanceof Number n) {
            userRepository.findById(n.longValue())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID));
        } else if (value instanceof String s) {
            userRepository.findById(Long.parseLong(s))
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_INVALID));
        }
    }
}
