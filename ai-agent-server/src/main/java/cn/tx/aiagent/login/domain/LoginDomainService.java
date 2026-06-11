package cn.tx.aiagent.login.domain;

import cn.tx.aiagent.user.domain.UserRepository;

public class LoginDomainService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public LoginDomainService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public AuthenticationResult authenticate(LoginCredential credential) {
        return userRepository.findByUsername(credential.username())
                .map(user -> {
                    if (!user.isActive()) {
                        return (AuthenticationResult) new AuthenticationResult.AccountDisabled();
                    }
                    if (passwordHasher.matches(credential.rawPassword(), user.getPasswordHash())) {
                        return new AuthenticationResult.Authenticated(user.getId(), user.getUsername(), user.getName());
                    }
                    return new AuthenticationResult.InvalidCredentials();
                })
                .orElseGet(AuthenticationResult.InvalidCredentials::new);
    }

    /**
     * 修改用户密码。验证旧密码正确性，校验新旧密码不同，然后哈希并更新新密码。
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        cn.tx.aiagent.user.domain.User user = userRepository.findById(userId)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.USER_NOT_FOUND, userId));

        if (!passwordHasher.matches(oldPassword, user.getPasswordHash())) {
            throw new cn.tx.aiagent.common.exception.BusinessException(
                    cn.tx.aiagent.common.api.ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        if (oldPassword.equals(newPassword)) {
            throw new cn.tx.aiagent.common.exception.BusinessException(
                    cn.tx.aiagent.common.api.ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        String newHash = passwordHasher.hash(newPassword);
        userRepository.updatePassword(userId, newHash);
        userRepository.updateMustChangePassword(userId, 0);
    }
}
