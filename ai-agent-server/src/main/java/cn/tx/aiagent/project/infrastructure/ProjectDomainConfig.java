package cn.tx.aiagent.project.infrastructure;

import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.project.domain.ProjectDomainService;
import cn.tx.aiagent.project.domain.ProjectRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 将领域服务注册为 Spring Bean，保持领域层零框架注解 */
@Configuration
public class ProjectDomainConfig {

    @Bean
    public ProjectDomainService projectDomainService(ProjectRepository projectRepository,
                                                      UserRepository userRepository) {
        return new ProjectDomainService(projectRepository, userRepository);
    }
}
