package cn.tx.aiagent.tempworker.infrastructure;

import cn.tx.aiagent.tempworker.domain.TempWorkerDomainService;
import cn.tx.aiagent.tempworker.domain.TempWorkerRepository;
import cn.tx.aiagent.user.domain.UserDomainService;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.user.domain.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TempWorkerDomainConfig {
    @Bean
    public TempWorkerDomainService tempWorkerDomainService(
            TempWorkerRepository tempWorkerRepository,
            UserDomainService userDomainService,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        return new TempWorkerDomainService(tempWorkerRepository, userDomainService, userRepository, roleRepository);
    }
}
