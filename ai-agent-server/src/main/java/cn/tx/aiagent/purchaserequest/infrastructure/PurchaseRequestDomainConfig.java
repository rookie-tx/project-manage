package cn.tx.aiagent.purchaserequest.infrastructure;

import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestDomainService;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseRequestDomainConfig {

    @Bean
    public PurchaseRequestDomainService purchaseRequestDomainService(PurchaseRequestRepository repository) {
        return new PurchaseRequestDomainService(repository);
    }
}
