package cn.tx.aiagent.user.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.domain.PasswordHasher;

import java.util.List;

/**
 * 用户管理领域服务。
 * 封装用户 CRUD 相关的业务规则。
 */
public class UserDomainService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * 创建用户，检查用户名唯一性，对密码进行哈希。
     */
    public User createUser(String username, String name, String rawPassword, String email, String phone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE, username);
        }
        String passwordHash = passwordHasher.hash(rawPassword);
        User user = User.create(username, name, passwordHash, email, phone);
        return userRepository.save(user);
    }

    /**
     * 更新用户信息。仅更新非 null 的字段。
     */
    public void updateUser(Long userId, String name, String email, String phone, Integer statusCode, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_DELETED, userId);
        }

        if (statusCode != null) {
            user.updateStatus(UserStatus.fromCode(statusCode));
        }

        user.updateProfile(name, email, phone);

        userRepository.update(user);

        if (roleIds != null) {
            userRepository.replaceUserRoles(userId, roleIds);
        }
    }

    /**
     * 软删除用户。
     */
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));

        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_DELETED, userId);
        }

        user.markDeleted();
        userRepository.softDelete(userId);
    }
}
