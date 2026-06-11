package cn.tx.aiagent.user.application;

import cn.tx.aiagent.user.domain.RoleRepository;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserDomainService;
import cn.tx.aiagent.user.domain.UserListResult;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.user.domain.UserSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理应用服务，编排 CRUD 操作和事务管理。
 */
@Service
public class UserApplicationService {

    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserApplicationService(UserDomainService userDomainService,
                                   UserRepository userRepository,
                                   RoleRepository roleRepository) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * 分页查询用户列表，附带角色信息。
     */
    public UserListResult listUsers(String keyword, Integer statusCode, int page, int size) {
        UserSearchCriteria criteria = new UserSearchCriteria(keyword, statusCode, page, size);
        UserListResult result = userRepository.findAll(criteria);
        // 为每个用户填充角色信息
        for (User user : result.records()) {
            user.setRoles(roleRepository.findByUserId(user.getId()));
        }
        return result;
    }

    /**
     * 查询单个用户详情，附带角色信息。
     */
    public User getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.USER_NOT_FOUND, userId));
        if (user.isDeleted()) {
            throw new cn.tx.aiagent.common.exception.BusinessException(
                    cn.tx.aiagent.common.api.ErrorCode.USER_DELETED, userId);
        }
        user.setRoles(roleRepository.findByUserId(userId));
        return user;
    }

    /**
     * 创建用户并可分配角色。
     */
    @Transactional
    public User createUser(String username, String name, String rawPassword, String email, String phone, List<Long> roleIds) {
        User user = userDomainService.createUser(username, name, rawPassword, email, phone);
        // 重新查询获取生成的 ID
        User saved = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("创建用户后查询失败"));
        if (roleIds != null && !roleIds.isEmpty()) {
            userRepository.replaceUserRoles(saved.getId(), roleIds);
        }
        return saved;
    }

    /**
     * 更新用户状态和角色。
     */
    @Transactional
    public void updateUser(Long userId, String name, String email, String phone, Integer statusCode, List<Long> roleIds) {
        userDomainService.updateUser(userId, name, email, phone, statusCode, roleIds);
        if (roleIds != null) {
            userRepository.replaceUserRoles(userId, roleIds);
        }
    }

    /**
     * 软删除用户。
     */
    @Transactional
    public void deleteUser(Long userId) {
        userDomainService.softDeleteUser(userId);
    }
}
