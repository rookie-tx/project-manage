package cn.tx.aiagent.user.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 MyBatis Mapper，SQL 定义在 mapper/RoleMapper.xml。
 */
@Mapper
public interface RoleMapper {

    List<RolePO> selectAll();

    RolePO selectById(@Param("id") Long id);

    List<RolePO> selectByUserId(@Param("userId") Long userId);

    RolePO selectByCode(@Param("code") String code);

    void insert(RolePO po);

    void update(RolePO po);

    void deleteById(@Param("id") Long id);

    int countUsersByRoleId(@Param("roleId") Long roleId);
}
