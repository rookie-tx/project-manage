package cn.tx.aiagent.supplier.infrastructure;

import cn.tx.aiagent.login.domain.PasswordHasher;
import cn.tx.aiagent.supplier.domain.SupplierDomainService;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import cn.tx.aiagent.user.domain.RoleRepository;
import cn.tx.aiagent.user.domain.UserDomainService;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.wechat.domain.WechatUserBindingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupplierDomainConfig {

    @Bean
    public SupplierDomainService supplierDomainService(
            SupplierRepository supplierRepository,
            UserDomainService userDomainService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            WechatUserBindingRepository bindingRepository,
            PasswordHasher passwordHasher) {
        return new SupplierDomainService(supplierRepository, userDomainService,
                userRepository, roleRepository, bindingRepository, passwordHasher);
    }
}
