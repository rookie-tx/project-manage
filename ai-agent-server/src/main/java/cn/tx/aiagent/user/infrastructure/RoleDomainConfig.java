package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.PermissionRepository;
import cn.tx.aiagent.user.domain.RoleDomainService;
import cn.tx.aiagent.user.domain.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 角色管理模块领域配置。
 */
@Configuration
public class RoleDomainConfig {

    @Bean
    public RoleDomainService roleDomainService(RoleRepository roleRepository,
                                                PermissionRepository permissionRepository) {
        return new RoleDomainService(roleRepository, permissionRepository);
    }
}
