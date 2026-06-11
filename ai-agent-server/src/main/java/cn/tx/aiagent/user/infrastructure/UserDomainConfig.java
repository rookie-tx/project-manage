package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.login.domain.PasswordHasher;
import cn.tx.aiagent.user.domain.UserDomainService;
import cn.tx.aiagent.user.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户管理模块领域配置，暴露领域服务为 Spring Bean。
 */
@Configuration
public class UserDomainConfig {

    @Bean
    public UserDomainService userDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new UserDomainService(userRepository, passwordHasher);
    }
}
