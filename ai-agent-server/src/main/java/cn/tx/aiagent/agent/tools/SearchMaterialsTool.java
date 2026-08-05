package cn.tx.aiagent.agent.tools;

import cn.tx.aiagent.common.util.MapperUtils;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SearchMaterialsTool extends AbstractTool<SearchMaterialsTool.SearchMaterialRequest> {

    private final MaterialRepository materialRepository;

    public SearchMaterialsTool(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    String getId() {
        return "search_materials";
    }

    @Override
    String getDescription() {
        return "按名称或规格关键词模糊搜索物料。传入物料名称或规格关键词，返回匹配的物料列表（上限10条），每项包含id、编码、名称、规格和单位。结果超过1条时应列出让用户选择。";
    }

    @Override
    Object doCall(String toolInput, ToolContext toolContext) {
        SearchMaterialRequest request = MapperUtils.readValue(toolInput, SearchMaterialRequest.class);
        return materialRepository.findByNameOrSpecLike(request.keyword()).stream()
                .map(m -> new SearchMaterialResponse(m.getId(), m.getCode(), m.getName(), m.getSpec(), m.getUnit()))
                .toList();
    }

    @Override
    Class<SearchMaterialRequest> getArgumentType() {
        return SearchMaterialRequest.class;
    }

    /**
     * 物料摘要，供 Agent 工具搜索返回。
     */
    public record SearchMaterialResponse(Long id, String code, String name, String spec, String unit) {}

    public record SearchMaterialRequest(
            @ToolParam(description = "搜索关键词，用于匹配物料名称或规格") String keyword) {}
}
