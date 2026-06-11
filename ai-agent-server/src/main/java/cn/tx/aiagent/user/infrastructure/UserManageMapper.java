package cn.tx.aiagent.user.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 MyBatis Mapper（user 模块），SQL 定义在 mapper/UserManageMapper.xml。
 */
@Mapper
public interface UserManageMapper {

    void insert(UserPO po);

    UserPO selectById(@Param("id") Long id);

    UserPO selectByUsername(@Param("username") String username);

    List<UserPO> selectAll(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           @Param("offset") int offset,
                           @Param("size") int size);

    long countAll(@Param("keyword") String keyword,
                  @Param("status") Integer status);

    void update(UserPO po);

    void softDelete(@Param("id") Long id);

    List<UserPO> selectAllActive();

    void deleteUserRoles(@Param("userId") Long userId);

    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    void updateMustChangePassword(@Param("id") Long id, @Param("mustChangePassword") int mustChangePassword);
}
