package cn.tx.aiagent.common.config;

import cn.tx.aiagent.login.application.SessionManager;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.user.domain.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Optional;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final SessionManager sessionManager;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public AuthenticationInterceptor(SessionManager sessionManager,
                                      RoleRepository roleRepository,
                                      PermissionRepository permissionRepository,
                                      UserRepository userRepository) {
        this.sessionManager = sessionManager;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                              Object handler) {
        // 如果 JwtAuthenticationFilter 已注入 LoginContext，跳过 Session 读取
        if (LoginContextHolder.get() != null) {
            return true;
        }

        Optional<LoginContext> sessionCtx = sessionManager.get();
        if (sessionCtx.isPresent()) {
            LoginContext ctx = sessionCtx.get();
            // 加载角色和权限
            List<String> roles = roleRepository.findByUserId(ctx.userId()).stream()
                    .map(Role::getCode)
                    .toList();
            List<Long> roleIds = roleRepository.findByUserId(ctx.userId()).stream()
                    .map(Role::getId)
                    .toList();
            List<String> permissions = permissionRepository.findByRoleIds(roleIds).stream()
                    .map(Permission::getCode)
                    .distinct()
                    .toList();
            // 构造带完整信息的 LoginContext
            User user = userRepository.findById(ctx.userId()).orElse(null);
            boolean mustChangePwd = user != null && user.mustChangePassword();

            String name = user != null ? user.getName() : null;
            LoginContext enriched = new LoginContext(ctx.userId(), ctx.username(), name, roles, permissions, mustChangePwd);
            LoginContextHolder.set(enriched);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        LoginContextHolder.clear();
    }
}
