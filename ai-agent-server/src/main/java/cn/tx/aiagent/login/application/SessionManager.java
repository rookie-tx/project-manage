package cn.tx.aiagent.login.application;

import cn.tx.aiagent.login.domain.LoginContext;

import java.util.Optional;

public interface SessionManager {
    void save(LoginContext context);

    Optional<LoginContext> get();

    void remove();
}
