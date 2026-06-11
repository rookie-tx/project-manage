package cn.tx.aiagent.user.application;

import cn.tx.aiagent.user.domain.PermissionRepository;
import cn.tx.aiagent.user.domain.Role;
import cn.tx.aiagent.user.domain.RoleDomainService;
import cn.tx.aiagent.user.domain.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理应用服务。
 */
@Service
public class RoleApplicationService {

    private final RoleDomainService roleDomainService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleApplicationService(RoleDomainService roleDomainService,
                                   RoleRepository roleRepository,
                                   PermissionRepository permissionRepository) {
        this.roleDomainService = roleDomainService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    public Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.ROLE_NOT_FOUND, id));
    }

    @Transactional
    public Role createRole(String code, String name, String description, List<Long> permissionIds) {
        return roleDomainService.createRole(code, name, description, permissionIds);
    }

    @Transactional
    public void updateRole(Long id, String name, String description, List<Long> permissionIds) {
        roleDomainService.updateRole(id, name, description, permissionIds);
    }

    @Transactional
    public void deleteRole(Long id) {
        roleDomainService.deleteRole(id);
    }

    public List<Long> getRolePermissionIds(Long roleId) {
        return roleRepository.findPermissionIdsByRoleId(roleId);
    }
}
