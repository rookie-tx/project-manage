package cn.tx.aiagent.user.domain;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;

import java.util.List;

/**
 * 角色管理领域服务。
 */
public class RoleDomainService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleDomainService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * 创建角色，检查编码唯一性。
     */
    public Role createRole(String code, String name, String description, List<Long> permissionIds) {
        if (roleRepository.findByCode(code).isPresent()) {
            throw new BusinessException(ErrorCode.ROLE_CODE_DUPLICATE, code);
        }
        Role role = Role.fromPersistence(null, code, name, description, 0);
        Role saved = roleRepository.save(role);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleRepository.replaceRolePermissions(saved.getId(), permissionIds);
        }
        return saved;
    }

    /**
     * 更新角色名称、描述和权限。
     */
    public void updateRole(Long roleId, String name, String description, List<Long> permissionIds) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, roleId));

        roleRepository.update(roleId, name, description);
        if (permissionIds != null) {
            roleRepository.replaceRolePermissions(roleId, permissionIds);
        }
    }

    /**
     * 删除角色。受保护的角色不可删除；有关联用户的角色不可删除。
     * 先清理角色-权限关联，再删除角色本身。
     */
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, roleId));

        if (role.isProtected()) {
            throw new BusinessException(ErrorCode.ROLE_PROTECTED);
        }

        int userCount = roleRepository.countUsersByRoleId(roleId);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.ROLE_HAS_USERS, userCount);
        }

        roleRepository.deleteById(roleId);
    }
}
