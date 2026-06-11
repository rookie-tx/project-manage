package cn.tx.aiagent.common.config;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.login.domain.LoginContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 强制改密拦截器：mustChangePassword 为 true 的用户只能访问登录/登出/改密/当前用户接口。
 */
@Component
public class ForcePasswordChangeInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 不受强制改密限制的路径 */
    private static final String[] ALLOWED_PATHS = {
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/password",
            "/api/auth/me"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) throws Exception {
        LoginContext ctx = LoginContextHolder.get();
        if (ctx == null || !ctx.mustChangePassword()) {
            return true;
        }

        String path = request.getRequestURI();
        for (String allowed : ALLOWED_PATHS) {
            if (path.equals(allowed)) {
                return true;
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<Void> result = Result.error(ErrorCode.MUST_CHANGE_PASSWORD);
        objectMapper.writeValue(response.getWriter(), result);
        return false;
    }
}
