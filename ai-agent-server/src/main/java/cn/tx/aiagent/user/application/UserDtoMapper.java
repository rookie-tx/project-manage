package cn.tx.aiagent.user.application;

import cn.tx.aiagent.user.api.UserResponse;
import cn.tx.aiagent.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper: User Domain ↔ UserResponse DTO。
 * 角色名称列表由 Domain 实体的 getRoleNames() 方法提供。
 */
@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "roles", expression = "java(user.getRoleNames())")
    UserResponse toResponse(User user);
}
