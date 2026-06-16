package cn.tx.aiagent.warehouse.infrastructure;

import cn.tx.aiagent.warehouse.domain.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaterialPoMapper {
    default Material toDomain(MaterialPO po) {
        if (po == null) return null;
        return Material.fromPersistence(po.getId(), po.getCode(), po.getName(), po.getSpec(), po.getUnit(),
                po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "unit", expression = "java(m.getUnit())")
    MaterialPO toPO(Material m);
}
