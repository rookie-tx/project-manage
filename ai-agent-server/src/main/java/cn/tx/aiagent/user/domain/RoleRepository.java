package cn.tx.aiagent.user.domain;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口。
 */
public interface RoleRepository {

    /** 查询所有角色 */
    List<Role> findAll();

    /** 按主键查询 */
    Optional<Role> findById(Long id);

    /** 按编码查询 */
    Optional<Role> findByCode(String code);

    /** 按用户 ID 查询该用户的所有角色 */
    List<Role> findByUserId(Long userId);

    /** 新增角色，返回带有自增 ID 的角色 */
    Role save(Role role);

    /** 更新角色名称和描述 */
    void update(Long roleId, String name, String description);

    /** 删除角色 */
    void deleteById(Long id);

    /** 替换角色权限关联 */
    void replaceRolePermissions(Long roleId, List<Long> permissionIds);

    /** 查询角色的权限 ID 列表 */
    List<Long> findPermissionIdsByRoleId(Long roleId);

    /** 统计拥有该角色的用户数量 */
    int countUsersByRoleId(Long roleId);
}
