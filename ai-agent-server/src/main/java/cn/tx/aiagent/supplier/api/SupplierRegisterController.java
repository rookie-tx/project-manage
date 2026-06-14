package cn.tx.aiagent.supplier.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.common.security.JwtTokenProvider;
import cn.tx.aiagent.login.domain.LoginContext;
import cn.tx.aiagent.supplier.application.SupplierApplicationService;
import cn.tx.aiagent.supplier.application.SupplierDtoMapper;
import cn.tx.aiagent.supplier.domain.SupplierDomainService;
import cn.tx.aiagent.user.domain.*;
import cn.tx.aiagent.wechat.WechatApiClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商注册控制器（公开接口，小程序使用）。
 */
@RestController
@RequestMapping("/api/supplier")
public class SupplierRegisterController {

    private final WechatApiClient wechatApiClient;
    private final SupplierDomainService supplierDomainService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SupplierApplicationService applicationService;
    private final SupplierDtoMapper dtoMapper;

    public SupplierRegisterController(WechatApiClient wechatApiClient,
                                       SupplierDomainService supplierDomainService,
                                       JwtTokenProvider jwtTokenProvider,
                                       RoleRepository roleRepository,
                                       PermissionRepository permissionRepository,
                                       SupplierApplicationService applicationService,
                                       SupplierDtoMapper dtoMapper) {
        this.wechatApiClient = wechatApiClient;
        this.supplierDomainService = supplierDomainService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.applicationService = applicationService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping("/register")
    public Result<SupplierRegisterResponse> register(@Valid @RequestBody SupplierRegisterRequest request) {
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

        // 2. 注册供应商
        User user = supplierDomainService.register(
                session.openid(), session.unionid(),
                request.getCompanyName(), request.getLicenseNo(),
                request.getContactPerson(), request.getContactPhone(),
                request.getAddress());

        // 3. 获取供应商信息
        SupplierResponse supplierResponse = applicationService.getMyProfile(user.getId());

        // 4. 构建 LoginContext 并签发 JWT
        List<String> roleCodes = roleRepository.findByUserId(user.getId()).stream()
                .map(Role::getCode).toList();
        List<Long> roleIds = roleRepository.findByUserId(user.getId()).stream()
                .map(Role::getId).toList();
        List<String> permissions = permissionRepository.findByRoleIds(roleIds).stream()
                .map(Permission::getCode).distinct().toList();

        LoginContext ctx = new LoginContext(user.getId(), user.getUsername(), user.getName(),
                roleCodes, permissions, user.mustChangePassword());
        String token = jwtTokenProvider.createToken(ctx);

        return Result.success(SupplierRegisterResponse.success(
                token, user.getId(), user.getUsername(), user.getName(), permissions, supplierResponse));
    }
}
