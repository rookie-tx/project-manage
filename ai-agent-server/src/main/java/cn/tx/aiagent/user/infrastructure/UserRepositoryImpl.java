package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserListResult;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.user.domain.UserSearchCriteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现（user 模块）。
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserManageMapper userManageMapper;
    private final UserPoMapper poMapper;

    public UserRepositoryImpl(UserManageMapper userManageMapper, UserPoMapper poMapper) {
        this.userManageMapper = userManageMapper;
        this.poMapper = poMapper;
    }

    @Override
    public User save(User user) {
        UserPO po = poMapper.toPO(user);
        userManageMapper.insert(po);
        return poMapper.toDomain(po);
    }

    @Override
    public Optional<User> findById(Long id) {
        UserPO po = userManageMapper.selectById(id);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserPO po = userManageMapper.selectByUsername(username);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public UserListResult findAll(UserSearchCriteria criteria) {
        List<UserPO> pos = userManageMapper.selectAll(
                criteria.keyword(), criteria.statusCode(),
                criteria.offset(), criteria.size());
        long total = userManageMapper.countAll(criteria.keyword(), criteria.statusCode());
        List<User> users = pos.stream().map(poMapper::toDomain).toList();
        return new UserListResult(users, total, criteria.page(), criteria.size());
    }

    @Override
    public void update(User user) {
        userManageMapper.update(poMapper.toPO(user));
    }

    @Override
    public void softDelete(Long id) {
        userManageMapper.softDelete(id);
    }

    @Override
    public List<User> findAllActive() {
        return userManageMapper.selectAllActive().stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public void replaceUserRoles(Long userId, List<Long> roleIds) {
        userManageMapper.deleteUserRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userManageMapper.insertUserRoles(userId, roleIds);
        }
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userManageMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public void updatePassword(Long userId, String passwordHash) {
        userManageMapper.updatePassword(userId, passwordHash);
    }

    @Override
    public void updateMustChangePassword(Long userId, int mustChangePassword) {
        userManageMapper.updateMustChangePassword(userId, mustChangePassword);
    }
}
