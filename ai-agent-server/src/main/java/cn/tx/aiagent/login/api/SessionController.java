package cn.tx.aiagent.login.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.application.LoginApplicationService;
import cn.tx.aiagent.login.application.SessionApplicationService;
import cn.tx.aiagent.login.domain.LoginContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SessionController {

    private final SessionApplicationService sessionApplicationService;
    private final LoginApplicationService loginApplicationService;

    public SessionController(SessionApplicationService sessionApplicationService,
                             LoginApplicationService loginApplicationService) {
        this.sessionApplicationService = sessionApplicationService;
        this.loginApplicationService = loginApplicationService;
    }

    /**
     * 获取当前用户信息。
     * 从 LoginContextHolder 读取以确保 roles/permissions 被拦截器充实。
     */
    @GetMapping("/me")
    public Result<LoginResponse> me() {
        LoginContext ctx = LoginContextHolder.get();
        if (ctx != null) {
            LoginResponse resp = new LoginResponse(ctx.userId(), ctx.username(), ctx.roles(), ctx.permissions());
            resp.setName(ctx.name());
            resp.setMustChangePassword(ctx.mustChangePassword());
            return Result.success(resp);
        }
        throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        sessionApplicationService.logout();
        return Result.success();
    }

    /**
     * 自服务修改密码。
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
        }
        loginApplicationService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        sessionApplicationService.logout();
        return Result.success();
    }
}
