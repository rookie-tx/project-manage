package cn.tx.aiagent.user.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 MyBatis Mapper，SQL 定义在 mapper/PermissionMapper.xml。
 */
@Mapper
public interface PermissionMapper {

    List<PermissionPO> selectAll();

    List<PermissionPO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<PermissionPO> selectByRoleId(@Param("roleId") Long roleId);

    void deleteByRoleId(@Param("roleId") Long roleId);

    void insertBatch(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    List<Long> selectIdsByRoleId(@Param("roleId") Long roleId);
}
