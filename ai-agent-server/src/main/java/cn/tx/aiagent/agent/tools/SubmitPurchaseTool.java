package cn.tx.aiagent.agent.tools;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.util.MapperUtils;
import cn.tx.aiagent.project.domain.Project;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
import cn.tx.aiagent.task.application.TaskApplicationService;
import cn.tx.aiagent.task.domain.BusinessType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SubmitPurchaseTool extends AbstractTool<SubmitPurchaseTool.SubmitPurchaseRequest> {

    private final PurchaseRequestRepository prRepository;

    private final TaskApplicationService taskApplicationService;

    private final ProjectRepository projectRepository;


    public SubmitPurchaseTool(PurchaseRequestRepository prRepository, TaskApplicationService taskApplicationService, ProjectRepository projectRepository) {
        this.prRepository = prRepository;
        this.taskApplicationService = taskApplicationService;
        this.projectRepository = projectRepository;
    }

    @Override
    String getId() {
        return "submit_purchase";
    }

    @Override
    String getDescription() {
        return "将采购申请草稿提交审批。当用户明确说明：帮我提交、提交等时调用";
    }

    @Override
    Object doCall(String toolInput, @Nullable ToolContext toolContext) {
        SubmitPurchaseRequest params = MapperUtils.readValue(toolInput, SubmitPurchaseRequest.class);
        PurchaseRequest pr = prRepository.findById(params.prId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_NOT_FOUND));

        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            return "提交失败：当前采购申请状态为「" + pr.getStatus().getLabel() + "」，不允许提交审批。";
        }

        Project project = projectRepository.findById(pr.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_PROJECT_NOT_FOUND));

        Map<String, Object> variables = new HashMap<>();
        if (project.getProjectManagerId() != null) {
            variables.put("pmUserId", String.valueOf(project.getProjectManagerId()));
        }

        try {
            taskApplicationService.startProcess(BusinessType.PURCHASE_REQUEST, params.prId(), variables);
            return "采购申请 " + pr.getCode() + " 已成功提交审批，项目经理会尽快处理。";
        } catch (BusinessException e) {
            return "提交失败：" + e.getMessage();
        }
    }

    @Override
    Class<SubmitPurchaseRequest> getArgumentType() {
        return SubmitPurchaseRequest.class;
    }

    public record SubmitPurchaseRequest(
            @ToolParam(description = "采购申请ID，从 create_purchase 返回结果中获取。") Long prId) {}
}
