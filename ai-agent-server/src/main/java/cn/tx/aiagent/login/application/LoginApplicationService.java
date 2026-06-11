package cn.tx.aiagent.login.application;

import cn.tx.aiagent.login.domain.AuthenticationResult;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.login.domain.LoginCredential;
import cn.tx.aiagent.login.domain.LoginDomainService;
import org.springframework.stereotype.Service;

@Service
public class LoginApplicationService {

    private final LoginDomainService loginDomainService;
    private final SessionManager sessionManager;

    public LoginApplicationService(LoginDomainService loginDomainService, SessionManager sessionManager) {
        this.loginDomainService = loginDomainService;
        this.sessionManager = sessionManager;
    }

    public AuthenticationResult login(LoginCredential credential) {
        AuthenticationResult result = loginDomainService.authenticate(credential);
        if (result instanceof AuthenticationResult.Authenticated(long userId, String username, String name)) {
            sessionManager.save(new LoginContext(userId, username, name));
        }
        return result;
    }

    /**
     * 修改当前用户密码。
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        loginDomainService.changePassword(userId, oldPassword, newPassword);
    }
}
