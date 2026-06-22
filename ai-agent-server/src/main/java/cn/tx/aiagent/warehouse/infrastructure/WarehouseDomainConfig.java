package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.application.StockApplicationService;
import cn.tx.aiagent.warehouse.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseDomainConfig {
    @Bean
    public StockApplicationService stockApplicationService(
            StockRecordRepository stockRecordRepository,
            StockBalanceRepository stockBalanceRepository) {
        return new StockApplicationService(stockRecordRepository, stockBalanceRepository);
    }
}
