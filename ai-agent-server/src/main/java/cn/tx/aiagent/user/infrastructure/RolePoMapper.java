package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.Role;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper: RolePO ↔ Role。
 */
@Mapper(componentModel = "spring")
public interface RolePoMapper {

    default Role toDomain(RolePO po) {
        if (po == null) {
            return null;
        }
        return Role.fromPersistence(po.getId(), po.getCode(), po.getName(),
                po.getDescription(), po.getIsProtected() != null ? po.getIsProtected() : 0);
    }

    RolePO toPO(Role role);
}
