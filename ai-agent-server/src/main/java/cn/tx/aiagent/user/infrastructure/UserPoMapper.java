package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper: UserPO ↔ User。
 * toDomain 委托给 User.fromPersistence 处理枚举还原，
 * toPO 由 MapStruct 自动生成，枚举→code 通过表达式转换。
 */
@Mapper(componentModel = "spring")
public interface UserPoMapper {

    default User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }
        return User.fromPersistence(
                po.getId(), po.getUsername(), po.getName(), po.getPasswordHash(),
                po.getEmail(), po.getPhone(),
                po.getStatus(), po.getMustChangePassword() != null ? po.getMustChangePassword() : 0,
                po.getIsDeleted(), po.getCreatedAt()
        );
    }

    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().getCode() : null)")
    @Mapping(target = "isDeleted", expression = "java(user.getIsDeleted())")
    UserPO toPO(User user);
}
