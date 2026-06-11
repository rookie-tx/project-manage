package cn.tx.aiagent.login.application;

import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.login.domain.LoginContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionApplicationService {

    private final SessionManager sessionManager;

    public SessionApplicationService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 获取当前登录用户。
     * 统一从 LoginContextHolder 读取——JWT 和 Session 认证均由拦截器注入到此。
     */
    public Optional<LoginContext> currentUser() {
        return Optional.ofNullable(LoginContextHolder.get());
    }

    public void logout() {
        sessionManager.remove();
    }
}
