package cn.tx.aiagent.project.domain;

import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;

/**
 * 项目领域服务，处理项目生命周期中的核心业务逻辑。
 * 不依赖任何框架注解，由 {@link cn.tx.aiagent.project.infrastructure.ProjectDomainConfig} 注册为 Bean。
 */
public class ProjectDomainService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectDomainService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /** 创建项目，校验项目经理存在且为 ACTIVE 状态，校验失败抛出 IllegalArgumentException */
    public Project create(Project project) {
        validateManagerExists(project.getProjectManagerId());
        projectRepository.save(project);
        return project;
    }

    /**
     * 更新项目：先应用字段变更，再处理状态流转。
     * 仅当 command.statusCode 非 null 且与当前状态不同时才执行流转校验。
     * 校验失败抛出 IllegalArgumentException（经理无效）或 IllegalStateException（非法流转）。
     */
    public Project update(Project existing, ProjectUpdateCommand command) {
        if (command.projectManagerId() != null
                && !command.projectManagerId().equals(existing.getProjectManagerId())) {
            validateManagerExists(command.projectManagerId());
        }
        existing.update(command);
        if (command.statusCode() != null) {
            ProjectStatus targetStatus = ProjectStatus.fromCode(command.statusCode());
            if (targetStatus != existing.getStatus()) {
                existing.transitionTo(targetStatus);
            }
        }
        projectRepository.update(existing);
        return existing;
    }

    /** 软删除：标记 is_deleted=1 后持久化 */
    public void softDelete(Project project) {
        project.markDeleted();
        projectRepository.update(project);
    }

    private void validateManagerExists(Long managerId) {
        User user = userRepository.findById(managerId).orElse(null);
        if (user == null || !user.isActive()) {
            throw new IllegalArgumentException("Project manager not found or disabled: " + managerId);
        }
    }
}
