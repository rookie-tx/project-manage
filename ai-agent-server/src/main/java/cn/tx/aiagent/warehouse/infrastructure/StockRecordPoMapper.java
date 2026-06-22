package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.StockRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockRecordPoMapper {
    default StockRecord toDomain(StockRecordPO po) {
        if (po == null) return null;
        return StockRecord.fromPersistence(po.getId(), po.getWarehouseId(), po.getMaterialId(),
                po.getType(), po.getQuantity(), po.getPurchaseRequestId(), po.getProjectId(),
                po.getOperatorId(), po.getCreatedAt(),
                po.getPurchaseRequestCode(), po.getOperatorName());
    }

    @Mapping(target = "type", expression = "java(sr.getType() != null ? sr.getType().getCode() : null)")
    StockRecordPO toPO(StockRecord sr);
}
