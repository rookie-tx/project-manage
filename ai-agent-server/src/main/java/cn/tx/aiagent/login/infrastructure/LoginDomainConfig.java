package cn.tx.aiagent.login.infrastructure;

import cn.tx.aiagent.login.domain.LoginDomainService;
import cn.tx.aiagent.login.domain.PasswordHasher;
import cn.tx.aiagent.user.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginDomainConfig {

    @Bean
    public LoginDomainService loginDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new LoginDomainService(userRepository, passwordHasher);
    }
}
