package cn.tx.aiagent.user.domain;

import java.util.Optional;

/**
 * 用户管理仓储接口，定义领域层所需的数据访问能力。
 */
public interface UserRepository {

    /** 新增用户，返回带自增 ID 的用户 */
    User save(User user);

    /** 按主键查询，排除已删除 */
    Optional<User> findById(Long id);

    /** 按用户名查询，排除已删除 */
    Optional<User> findByUsername(String username);

    /** 分页/筛选查询用户列表 */
    UserListResult findAll(UserSearchCriteria criteria);

    /** 更新用户全部字段 */
    void update(User user);

    /** 软删除用户 */
    void softDelete(Long id);

    /** 查询所有活跃用户（用于下拉选择） */
    java.util.List<User> findAllActive();

    /** 替换用户角色关联（先删后插） */
    void replaceUserRoles(Long userId, java.util.List<Long> roleIds);

    /** 查询用户的角色 ID 列表 */
    java.util.List<Long> findRoleIdsByUserId(Long userId);

    /** 更新用户密码哈希 */
    void updatePassword(Long userId, String passwordHash);

    /** 更新首次登录改密标记 */
    void updateMustChangePassword(Long userId, int mustChangePassword);
}
