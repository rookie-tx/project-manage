package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.Permission;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: PermissionPO ↔ Permission。
 */
@Mapper(componentModel = "spring")
public interface PermissionPoMapper {

    default Permission toDomain(PermissionPO po) {
        if (po == null) {
            return null;
        }
        return Permission.fromPersistence(po.getId(), po.getCode(), po.getName(), po.getDescription());
    }

    PermissionPO toPO(Permission permission);
}
