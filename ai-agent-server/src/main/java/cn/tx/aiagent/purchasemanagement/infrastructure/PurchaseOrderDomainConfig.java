package cn.tx.aiagent.purchasemanagement.infrastructure;

import cn.tx.aiagent.file.domain.UploadFileRepository;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.purchasemanagement.application.PurchaseOrderApplicationService;
import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrderRepository;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestRepository;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.warehouse.domain.MaterialRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseOrderDomainConfig {

    @Bean
    public PurchaseOrderApplicationService purchaseOrderApplicationService(
            PurchaseOrderRepository repository,
            PurchaseRequestRepository purchaseRequestRepository,
            UploadFileRepository uploadFileRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MaterialRepository materialRepository) {
        return new PurchaseOrderApplicationService(repository, purchaseRequestRepository,
                uploadFileRepository, userRepository, projectRepository, materialRepository);
    }
}
