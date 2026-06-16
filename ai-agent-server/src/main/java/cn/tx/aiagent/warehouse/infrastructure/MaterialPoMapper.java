package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.purchaserequest.domain.Unit;
import cn.tx.aiagent.warehouse.domain.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaterialPoMapper {
    default Material toDomain(MaterialPO po) {
        if (po == null) return null;
        Unit unit = po.getUnit() != null ? Unit.fromCode(po.getUnit()) : null;
        return Material.fromPersistence(po.getId(), po.getCode(), po.getName(), po.getSpec(), unit,
                po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "unit", expression = "java(m.getUnit() != null ? m.getUnit().getCode() : null)")
    MaterialPO toPO(Material m);
}
