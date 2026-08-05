package cn.tx.aiagent.agent.tools;

import cn.tx.aiagent.common.util.MapperUtils;
import cn.tx.aiagent.project.domain.ProjectRepository;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SearchProjectsTool extends AbstractTool<SearchProjectsTool.SearchProjectRequest> {

    private final ProjectRepository projectRepository;

    public SearchProjectsTool(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    String getId() {
        return "search_projects";
    }

    @Override
    String getDescription() {
        return "按名称关键词模糊搜索项目。传入项目名称关键词，返回匹配的项目列表（上限10条），每项包含id、编码和名称。结果超过1条时应列出让用户选择。";
    }

    @Override
    Object doCall(String toolInput, ToolContext toolContext) {
        SearchProjectRequest request = MapperUtils.readValue(toolInput, SearchProjectRequest.class);
        return projectRepository.findByNameLike(request.keyword()).stream()
                .map(p -> new SearchProjectResponse(p.getId(), p.getCode(), p.getName()))
                .toList();
    }

    @Override
    Class<SearchProjectRequest> getArgumentType() {
        return SearchProjectRequest.class;
    }

    record SearchProjectResponse(Long id, String code, String name) {}

    record SearchProjectRequest(
            @ToolParam(description = "搜索关键词，用于匹配项目名称") String keyword) {}
}


