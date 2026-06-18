package cn.tx.aiagent.quotation.infrastructure;

import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.quotation.application.QuotationApplicationService;
import cn.tx.aiagent.quotation.application.QuotationDtoMapper;
import cn.tx.aiagent.quotation.domain.QuotationDomainService;
import cn.tx.aiagent.quotation.domain.QuotationRepository;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import cn.tx.aiagent.user.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 报价模块 Spring 配置
 */
@Configuration
public class QuotationDomainConfig {

    @Bean
    public QuotationDomainService quotationDomainService(
            QuotationRepository quotationRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        return new QuotationDomainService(quotationRepository, purchaseOrderRepository);
    }

    @Bean
    public QuotationApplicationService quotationApplicationService(
            QuotationDomainService domainService,
            QuotationRepository quotationRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            QuotationDtoMapper dtoMapper) {
        return new QuotationApplicationService(domainService, quotationRepository,
                purchaseOrderRepository, supplierRepository, userRepository, dtoMapper);
    }
}
