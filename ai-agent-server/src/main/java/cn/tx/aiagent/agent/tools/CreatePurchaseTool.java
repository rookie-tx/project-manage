package cn.tx.aiagent.agent.tools;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.util.MapperUtils;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchaserequest.api.PurchaseRequestResponse;
import cn.tx.aiagent.purchaserequest.application.PurchaseRequestApplicationService;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.warehouse.domain.Material;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
public class CreatePurchaseTool extends AbstractTool<CreatePurchaseTool.CreatePurchaseRequest> {

    private final ProjectRepository projectRepository;

    private final MaterialRepository materialRepository;

    private final PurchaseRequestApplicationService prApplicationService;

    public CreatePurchaseTool(ProjectRepository projectRepository,  MaterialRepository materialRepository,
                              PurchaseRequestApplicationService prApplicationService) {
        this.projectRepository = projectRepository;
        this.materialRepository = materialRepository;
        this.prApplicationService = prApplicationService;
    }

    @Override
    String getId() {
        return "create_purchase";
    }

    @Override
    String getDescription() {
        return """
                创建采购申请草稿（状态为待提交）。
                调用条件（必须全部满足）：①项目ID已确定 ②物料ID已确定 ③知道数量 ④知道到场日期。
                brand和remark是可选的，用户没提就不传，不要因为没有品牌就不调。
                创建后展示草稿等用户确认，用户确认后才能调 submitForApproval。
                只有在缺项目/缺物料/缺数量/缺日期时才追问，品牌和备注不算缺失信息。
                """;
    }

    @Override
    Object doCall(String toolInput, ToolContext toolContext) {
        log.info(toolInput);

        Long createdBy = getUserId(toolContext);

        CreatePurchaseRequest params = MapperUtils.readValue(toolInput, CreatePurchaseRequest.class);

        // 校验项目存在
        projectRepository.findById(params.projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PR_PROJECT_NOT_FOUND));

        // 校验物料存在
        Material material = materialRepository.findById(params.materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));

        String code = prApplicationService.generateCode();

        PurchaseRequest pr = PurchaseRequest.create(params.projectId(), params.materialId(), material.getName(),
                material.getSpec(), params.brand(), params.quantity(), material.getUnit(), params.arrivalDate(),
                params.remark(), createdBy);
        pr.setCode(code);
        pr.setCreatedBy(createdBy);

        PurchaseRequestResponse response;
        try {
            response = prApplicationService.create(pr);
        } catch (Exception e) {
            log.info("异常", e);
            throw e;
        }

        return new CreatePurchaseResponse(response.getId(), response.getCode(), response.getName(), response.getName(),
                response.getMaterialSpec(), response.getBrand(), response.getQuantity(), response.getMaterialUnit(),
                response.getArrivalDate().toString());
    }

    @Override
    Class<CreatePurchaseRequest> getArgumentType() {
        return CreatePurchaseRequest.class;
    }

    @Override
    @NonNull
    public ToolMetadata getToolMetadata() {
        return super.getToolMetadata();
    }

    /**
     * 采购申请草稿卡片，前端渲染为确认卡片（含操作按钮）。
     */
    public record CreatePurchaseResponse(
            Long prId,
            String prCode,
            String projectName,
            String materialName,
            String materialSpec,
            String brand,
            BigDecimal quantity,
            String unit,
            String arrivalDate) {}

    public record CreatePurchaseRequest(
            @ToolParam(description = "项目ID")
            Long projectId,
            @ToolParam(description = "物料ID")
            Long materialId,
            @ToolParam(description = "品牌名称")
            String brand,
            @ToolParam(description = "采购数量（数字）")
            BigDecimal quantity,
            @ToolParam(description = "期望到场日期，格式 yyyy-MM-dd，如\"2026-07-14\"", required = false)
            LocalDate arrivalDate,
            @ToolParam(description = "备注信息", required = false)
            String remark) {}
}
