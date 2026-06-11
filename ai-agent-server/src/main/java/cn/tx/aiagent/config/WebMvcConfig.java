package cn.tx.aiagent.config;

import cn.tx.aiagent.common.config.AuthenticationInterceptor;
import cn.tx.aiagent.common.config.ForcePasswordChangeInterceptor;
import cn.tx.aiagent.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final ForcePasswordChangeInterceptor forcePasswordChangeInterceptor;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor,
                         ForcePasswordChangeInterceptor forcePasswordChangeInterceptor,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.forcePasswordChangeInterceptor = forcePasswordChangeInterceptor;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 认证拦截器（最先执行，有 token 则注入 LoginContext，无 token 则放行）
        registry.addInterceptor(jwtAuthenticationFilter)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/wechat-login", "/api/auth/wechat-bind");
        // Session 认证拦截器（JWT 未注入时回退到 Session）
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/wechat-login", "/api/auth/wechat-bind");
        // 强制改密拦截（在认证拦截器之后执行，依赖 LoginContextHolder）
        registry.addInterceptor(forcePasswordChangeInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/logout", "/api/auth/password", "/api/auth/me");
    }
}
