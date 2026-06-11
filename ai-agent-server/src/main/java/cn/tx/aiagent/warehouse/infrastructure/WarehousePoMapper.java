package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehousePoMapper {
    default Warehouse toDomain(WarehousePO po) {
        if (po == null) return null;
        return Warehouse.fromPersistence(po.getId(), po.getCode(), po.getName(), po.getLocation(),
                po.getStatus(), po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "status", expression = "java(w.getStatus() != null ? w.getStatus().getCode() : null)")
    @Mapping(target = "isDeleted", expression = "java(w.getIsDeleted())")
    WarehousePO toPO(Warehouse w);
}
