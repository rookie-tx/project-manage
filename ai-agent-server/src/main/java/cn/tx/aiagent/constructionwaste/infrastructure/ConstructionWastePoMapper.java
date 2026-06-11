package cn.tx.aiagent.constructionwaste.infrastructure;

import cn.tx.aiagent.constructionwaste.domain.ConstructionWaste;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConstructionWastePoMapper {

    default ConstructionWaste toDomain(ConstructionWastePO po) {
        if (po == null) return null;
        return ConstructionWaste.fromPersistence(
                po.getId(), po.getProjectId(), po.getWasteType(),
                po.getQuantity(), po.getUnit(), po.getVehiclePlate(),
                po.getDriverName(), po.getDestination(), po.getRemovalDate(),
                po.getRemark(), po.getOperatorId(), po.getIsDeleted(),
                po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    ConstructionWastePO toPO(ConstructionWaste waste);
}
