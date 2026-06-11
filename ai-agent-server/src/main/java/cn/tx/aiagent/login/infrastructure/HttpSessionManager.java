package cn.tx.aiagent.login.infrastructure;

import cn.tx.aiagent.login.application.SessionManager;
import cn.tx.aiagent.login.domain.LoginContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class HttpSessionManager implements SessionManager {

    private static final String USER_ATTRIBUTE = "LOGIN_CONTEXT";

    @Override
    public void save(LoginContext context) {
        currentSession().setAttribute(USER_ATTRIBUTE, context);
    }

    @Override
    public Optional<LoginContext> get() {
        LoginContext context = (LoginContext) currentSession().getAttribute(USER_ATTRIBUTE);
        return Optional.ofNullable(context);
    }

    @Override
    public void remove() {
        currentSession().invalidate();
    }

    private HttpSession currentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }
}
