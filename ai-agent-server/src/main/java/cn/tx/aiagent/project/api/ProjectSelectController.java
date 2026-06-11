package cn.tx.aiagent.project.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.project.domain.Project;
import cn.tx.aiagent.project.domain.ProjectRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectSelectController {

    private final ProjectRepository projectRepository;

    public ProjectSelectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/projects/select")
    public Result<List<ProjectSelectResponse>> select() {
        List<ProjectSelectResponse> projects = projectRepository.findAllActive().stream()
                .map(p -> new ProjectSelectResponse(p.getId(), p.getCode(), p.getName()))
                .toList();
        return Result.success(projects);
    }
}
