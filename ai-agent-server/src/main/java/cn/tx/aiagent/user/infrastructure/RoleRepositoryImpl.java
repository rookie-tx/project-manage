package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.PermissionRepository;
import cn.tx.aiagent.user.domain.Role;
import cn.tx.aiagent.user.domain.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储实现。
 */
@Component
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    private final RolePoMapper poMapper;

    public RoleRepositoryImpl(RoleMapper roleMapper, PermissionRepository permissionRepository, RolePoMapper poMapper) {
        this.roleMapper = roleMapper;
        this.permissionRepository = permissionRepository;
        this.poMapper = poMapper;
    }

    @Override
    public List<Role> findAll() {
        return roleMapper.selectAll().stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Role> findById(Long id) {
        RolePO po = roleMapper.selectById(id);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<Role> findByCode(String code) {
        RolePO po = roleMapper.selectByCode(code);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<Role> findByUserId(Long userId) {
        return roleMapper.selectByUserId(userId).stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public Role save(Role role) {
        RolePO po = poMapper.toPO(role);
        roleMapper.insert(po);
        return poMapper.toDomain(po);
    }

    @Override
    public void update(Long roleId, String name, String description) {
        RolePO po = new RolePO();
        po.setId(roleId);
        po.setName(name);
        po.setDescription(description);
        roleMapper.update(po);
    }

    @Override
    public void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        permissionRepository.deleteByRoleId(roleId);
        permissionRepository.saveBatch(roleId, permissionIds);
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return permissionRepository.findPermissionIdsByRoleId(roleId);
    }

    @Override
    public int countUsersByRoleId(Long roleId) {
        return roleMapper.countUsersByRoleId(roleId);
    }

    @Override
    public void deleteById(Long id) {
        permissionRepository.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }
}
