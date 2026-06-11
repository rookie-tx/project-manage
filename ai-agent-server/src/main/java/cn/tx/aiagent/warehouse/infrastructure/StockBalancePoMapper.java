package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.StockBalance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockBalancePoMapper {
    default StockBalance toDomain(StockBalancePO po) {
        if (po == null) return null;
        return StockBalance.fromPersistence(po.getId(), po.getWarehouseId(), po.getMaterialId(), po.getQuantity());
    }
    StockBalancePO toPO(StockBalance sb);
}
