package cn.tx.aiagent.common.security;

import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.login.domain.LoginContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthenticationFilter implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            if (jwtTokenProvider.validateToken(token)) {
                LoginContext ctx = jwtTokenProvider.parseToken(token);
                LoginContextHolder.set(ctx);
            }
        }
        // 无 token 或有 token 无效时放行，回退到 Session 认证
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        // JwtAuthenticationFilter 不应清除 LoginContextHolder，
        // 因为后续可能有 AuthenticationInterceptor 需要读取 Session。
        // 清除由 AuthenticationInterceptor.afterCompletion 负责。
    }
}
