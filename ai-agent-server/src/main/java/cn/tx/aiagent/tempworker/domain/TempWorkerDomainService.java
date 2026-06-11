package cn.tx.aiagent.tempworker.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.user.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public class TempWorkerDomainService {
    private final TempWorkerRepository tempWorkerRepository;
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public TempWorkerDomainService(TempWorkerRepository tempWorkerRepository,
                                    UserDomainService userDomainService,
                                    UserRepository userRepository,
                                    RoleRepository roleRepository) {
        this.tempWorkerRepository = tempWorkerRepository;
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /** 创建临时工：创建用户 + 赋角色 + 创建临时工记录 */
    @Transactional(rollbackFor = Exception.class)
    public User create(String phone, String name, Long projectId, String workerType, LocalDate entryDate) {
        if (userRepository.findByUsername(phone).isPresent()) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE, phone);
        }
        User user = userDomainService.createUser(phone, name, "123456", null, phone);
        userRepository.updateMustChangePassword(user.getId(), 1);

        // 分配临时工角色
        roleRepository.findByCode("temp_worker").ifPresent(role ->
            userRepository.replaceUserRoles(user.getId(), List.of(role.getId()))
        );

        TempWorker worker = TempWorker.create(user.getId(), projectId, workerType, entryDate);
        tempWorkerRepository.save(worker);
        return user;
    }

    /** 离职：设置离职日期 + 禁用用户 */
    public void leave(Long id, LocalDate exitDate) {
        TempWorker worker = tempWorkerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEMP_WORKER_NOT_FOUND, id));
        worker.markLeft(exitDate);
        tempWorkerRepository.update(worker);
        // 禁用关联用户
        User user = userRepository.findById(worker.getUserId())
                .orElse(null);
        if (user != null) {
            userRepository.update(createDisabledUser(user));
        }
    }

    private User createDisabledUser(User user) {
        User copy = User.fromPersistence(
                user.getId(), user.getUsername(), user.getName(), user.getPasswordHash(),
                user.getEmail(), user.getPhone(), UserStatus.DISABLED.getCode(), 0,
                user.getIsDeleted(), user.getCreatedAt());
        return copy;
    }
}
