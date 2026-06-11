package cn.tx.aiagent.login.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.application.LoginApplicationService;
import cn.tx.aiagent.login.domain.AuthenticationResult;
import cn.tx.aiagent.login.domain.LoginCredential;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class LoginController {

    private final LoginApplicationService loginApplicationService;

    public LoginController(LoginApplicationService loginApplicationService) {
        this.loginApplicationService = loginApplicationService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        LoginCredential credential = new LoginCredential(request.getUsername(), request.getPassword());
        AuthenticationResult result = loginApplicationService.login(credential);

        return switch (result) {
            case AuthenticationResult.Authenticated auth ->
                    Result.success(new LoginResponse(auth.userId(), auth.username(), auth.name()));
            case AuthenticationResult.AccountDisabled __ ->
                    throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            case AuthenticationResult.InvalidCredentials __ ->
                    throw new BusinessException(ErrorCode.CREDENTIALS_INVALID);
        };
    }
}
