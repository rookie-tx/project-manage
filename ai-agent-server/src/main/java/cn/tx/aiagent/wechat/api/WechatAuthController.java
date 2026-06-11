package cn.tx.aiagent.wechat.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.JwtTokenProvider;
import cn.tx.aiagent.login.domain.*;
import cn.tx.aiagent.user.domain.*;
import cn.tx.aiagent.wechat.WechatApiClient;
import cn.tx.aiagent.wechat.domain.WechatUserBinding;
import cn.tx.aiagent.wechat.domain.WechatUserBindingRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class WechatAuthController {

    private final WechatApiClient wechatApiClient;
    private final WechatUserBindingRepository bindingRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginDomainService loginDomainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public WechatAuthController(WechatApiClient wechatApiClient,
                                 WechatUserBindingRepository bindingRepository,
                                 JwtTokenProvider jwtTokenProvider,
                                 LoginDomainService loginDomainService,
                                 UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 PermissionRepository permissionRepository) {
        this.wechatApiClient = wechatApiClient;
        this.bindingRepository = bindingRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginDomainService = loginDomainService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @PostMapping("/wechat-login")
    public Result<WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        // 1. 微信 code → openid
        WechatApiClient.WechatSession session;
        try {
            session = wechatApiClient.code2Session(request.getCode());
        } catch (WechatApiClient.WechatApiException e) {
            if (e.getErrcode() == 40029 || e.getErrcode() == 40163) {
                throw new BusinessException(ErrorCode.WECHAT_CODE_INVALID);
            }
            throw new BusinessException(ErrorCode.WECHAT_API_ERROR);
        }

        // 2. 查找绑定
        return bindingRepository.findByOpenid(session.openid())
                .map(binding -> {
                    // 已绑定：查用户信息签发 JWT
                    User user = userRepository.findById(binding.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, binding.getUserId()));
                    if (!user.isActive()) {
                        throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
                    }
                    LoginContext ctx = buildLoginContext(user);
                    String token = jwtTokenProvider.createToken(ctx);
                    return Result.success(WechatLoginResponse.success(token, user.getId(), user.getUsername(), user.getName(), ctx.permissions()));
                })
                .orElseGet(() -> Result.success(WechatLoginResponse.needBind()));
    }

    @PostMapping("/wechat-bind")
    public Result<WechatLoginResponse> wechatBind(@Valid @RequestBody WechatBindRequest request) {
        // 1. 微信 code → openid
        WechatApiClient.WechatSession session;
        try {
            session = wechatApiClient.code2Session(request.getCode());
        } catch (WechatApiClient.WechatApiException e) {
            if (e.getErrcode() == 40029 || e.getErrcode() == 40163) {
                throw new BusinessException(ErrorCode.WECHAT_CODE_INVALID);
            }
            throw new BusinessException(ErrorCode.WECHAT_API_ERROR);
        }

        // 2. 检查 openid 是否已绑定
        if (bindingRepository.findByOpenid(session.openid()).isPresent()) {
            throw new BusinessException(ErrorCode.WECHAT_ALREADY_BOUND);
        }

        // 3. 验证用户名密码
        AuthenticationResult result = loginDomainService.authenticate(
                new LoginCredential(request.getUsername(), request.getPassword()));
        if (!(result instanceof AuthenticationResult.Authenticated(long userId, String username, String name))) {
            throw new BusinessException(ErrorCode.CREDENTIALS_INVALID);
        }

        // 4. 创建绑定
        WechatUserBinding binding = WechatUserBinding.create(userId, session.openid(), session.unionid());
        bindingRepository.save(binding);

        // 5. 签发 JWT
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
        LoginContext ctx = buildLoginContext(user);
        String token = jwtTokenProvider.createToken(ctx);

        return Result.success(WechatLoginResponse.success(token, userId, username, user.getName(), ctx.permissions()));
    }

    @DeleteMapping("/wechat-unbind")
    public Result<Void> wechatUnbind() {
        // 从 LoginContextHolder 获取当前用户（JWT 或 Session 都已注入）
        LoginContext ctx = cn.tx.aiagent.common.config.LoginContextHolder.get();
        if (ctx == null) {
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
        }

        if (bindingRepository.findByUserId(ctx.userId()).isEmpty()) {
            throw new BusinessException(ErrorCode.WECHAT_NOT_BOUND);
        }

        bindingRepository.deleteByUserId(ctx.userId());
        return Result.success();
    }

    private LoginContext buildLoginContext(User user) {
        List<String> roles = roleRepository.findByUserId(user.getId()).stream()
                .map(Role::getCode)
                .toList();
        List<Long> roleIds = roleRepository.findByUserId(user.getId()).stream()
                .map(Role::getId)
                .toList();
        List<String> permissions = permissionRepository.findByRoleIds(roleIds).stream()
                .map(Permission::getCode)
                .distinct()
                .toList();
        return new LoginContext(user.getId(), user.getUsername(), user.getName(),
                roles, permissions, user.mustChangePassword());
    }
}
