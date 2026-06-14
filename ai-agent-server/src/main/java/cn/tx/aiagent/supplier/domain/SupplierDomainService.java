package cn.tx.aiagent.supplier.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.domain.PasswordHasher;
import cn.tx.aiagent.user.domain.*;
import cn.tx.aiagent.wechat.domain.WechatUserBinding;
import cn.tx.aiagent.wechat.domain.WechatUserBindingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * 供应商领域服务。
 * 封装供应商 CRUD 和注册相关的业务规则。
 */
public class SupplierDomainService {

    private final SupplierRepository supplierRepository;
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WechatUserBindingRepository bindingRepository;
    private final PasswordHasher passwordHasher;

    public SupplierDomainService(SupplierRepository supplierRepository,
                                  UserDomainService userDomainService,
                                  UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  WechatUserBindingRepository bindingRepository,
                                  PasswordHasher passwordHasher) {
        this.supplierRepository = supplierRepository;
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bindingRepository = bindingRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * 供应商注册：创建用户 → 绑定微信 → 创建供应商 → 返回用户。
     * 一步完成，无需审批。
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(String openid, String unionid, String companyName, String licenseNo,
                          String contactPerson, String contactPhone, String address) {
        // 1. 检查 openid 是否已绑定
        if (bindingRepository.findByOpenid(openid).isPresent()) {
            throw new BusinessException(ErrorCode.WECHAT_ALREADY_BOUND);
        }

        // 2. 生成 username（supplier_ + openid后12位）
        String username = generateSupplierUsername(openid);

        // 3. 生成随机密码
        String rawPassword = generateRandomPassword();
        String passwordHash = passwordHasher.hash(rawPassword);

        // 4. 创建用户
        User newUser = User.create(username, contactPerson, passwordHash, null, contactPhone);
        final User savedUser = userRepository.save(newUser);

        // 5. 分配 supplier 角色
        roleRepository.findByCode("supplier").ifPresent(role ->
            userRepository.replaceUserRoles(savedUser.getId(), List.of(role.getId()))
        );

        // 6. 创建微信绑定
        WechatUserBinding binding = WechatUserBinding.create(savedUser.getId(), openid, unionid);
        bindingRepository.save(binding);

        // 7. 创建供应商记录
        Supplier supplier = Supplier.create(savedUser.getId(), companyName, licenseNo,
                contactPerson, contactPhone, address);
        supplierRepository.save(supplier);

        // 8. 返回用户（带有角色信息）
        return userRepository.findById(savedUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, savedUser.getId()));
    }

    /** 管理员创建供应商 */
    @Transactional(rollbackFor = Exception.class)
    public Supplier createByAdmin(String username, String rawPassword,
                                   String companyName, String licenseNo,
                                   String contactPerson, String contactPhone,
                                   String address) {
        // 1. 创建用户
        User user = userDomainService.createUser(username, contactPerson, rawPassword, null, contactPhone);

        // 2. 分配 supplier 角色
        roleRepository.findByCode("supplier").ifPresent(role ->
            userRepository.replaceUserRoles(user.getId(), List.of(role.getId()))
        );

        // 3. 创建供应商记录
        Supplier supplier = Supplier.create(user.getId(), companyName, licenseNo,
                contactPerson, contactPhone, address);
        supplierRepository.save(supplier);
        return supplier;
    }

    /** 更新供应商 */
    public void updateSupplier(Long id, String companyName, String licenseNo,
                                String contactPerson, String contactPhone,
                                String address, String remark, Integer statusCode) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND, id));

        supplier.update(companyName, licenseNo, contactPerson, contactPhone, address, remark);

        if (statusCode != null) {
            supplier.changeStatus(SupplierStatus.fromCode(statusCode));
        }

        supplierRepository.update(supplier);
    }

    /** 更新自身资料（不允许修改公司名称和信用代码） */
    public void updateProfile(Long userId, String contactPerson, String contactPhone, String address) {
        Supplier supplier = supplierRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND, userId));
        supplier.updateProfile(contactPerson, contactPhone, address);
        supplierRepository.update(supplier);
    }

    /** 软删除供应商 */
    public void softDelete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUPPLIER_NOT_FOUND, id));

        supplier.markDeleted();
        supplierRepository.softDelete(id);

        // 同时禁用关联用户
        User user = userRepository.findById(supplier.getUserId()).orElse(null);
        if (user != null) {
            user.updateStatus(UserStatus.DISABLED);
            userRepository.update(user);
        }
    }

    /** 生成供应商用户名 */
    private String generateSupplierUsername(String openid) {
        String suffix = openid.length() > 12 ? openid.substring(openid.length() - 12) : openid;
        String username = "supplier_" + suffix;
        // 冲突时追加随机后缀
        SecureRandom rng = new SecureRandom();
        while (userRepository.findByUsername(username).isPresent()) {
            username = "supplier_" + suffix + "_" + rng.nextInt(1000);
        }
        return username;
    }

    /** 生成随机密码 */
    private String generateRandomPassword() {
        SecureRandom rng = new SecureRandom();
        byte[] bytes = new byte[12];
        rng.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
