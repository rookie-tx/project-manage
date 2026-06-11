package cn.tx.aiagent.user.domain;

import java.util.List;

/**
 * 权限仓储接口。
 */
public interface PermissionRepository {

    /** 查询所有权限 */
    List<Permission> findAll();

    /** 按角色 ID 列表查询权限 */
    List<Permission> findByRoleIds(List<Long> roleIds);

    /** 按单个角色 ID 查询权限 */
    List<Permission> findByRoleId(Long roleId);

    /** 删除角色关联的所有权限 */
    void deleteByRoleId(Long roleId);

    /** 批量插入角色权限关联 */
    void saveBatch(Long roleId, List<Long> permissionIds);

    /** 查询角色关联的权限 ID 列表 */
    List<Long> findPermissionIdsByRoleId(Long roleId);
}
