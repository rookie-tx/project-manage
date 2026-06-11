package cn.tx.aiagent.common.config;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.domain.LoginContext;

import java.util.List;

public class LoginContextHolder {

    private static final ThreadLocal<LoginContext> CONTEXT = new ThreadLocal<>();

    public static void set(LoginContext context) {
        CONTEXT.set(context);
    }

    public static LoginContext get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.userId() : null;
    }

    public static String getUsername() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.username() : null;
    }

    public static String getName() {
        LoginContext ctx = CONTEXT.get();
        if (ctx == null) return null;
        return ctx.name() != null ? ctx.name() : ctx.username();
    }

    public static List<String> getRoles() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.roles() : java.util.Collections.emptyList();
    }

    public static List<String> getPermissions() {
        LoginContext ctx = CONTEXT.get();
        return ctx != null ? ctx.permissions() : java.util.Collections.emptyList();
    }

    /**
     * 获取当前登录上下文，未认证时抛出 NOT_AUTHENTICATED。
     * 统一入口——JWT 和 Session 认证都由各自的拦截器注入到此 ThreadLocal。
     */
    public static LoginContext require() {
        LoginContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
        }
        return ctx;
    }

    /**
     * 获取当前登录用户 ID，未认证时抛出 NOT_AUTHENTICATED。
     */
    public static Long requireUserId() {
        return require().userId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
