package cn.tx.aiagent.user.infrastructure;

import cn.tx.aiagent.user.domain.Permission;
import cn.tx.aiagent.user.domain.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限仓储实现。
 */
@Component
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;
    private final PermissionPoMapper poMapper;

    public PermissionRepositoryImpl(PermissionMapper permissionMapper, PermissionPoMapper poMapper) {
        this.permissionMapper = permissionMapper;
        this.poMapper = poMapper;
    }

    @Override
    public List<Permission> findAll() {
        return permissionMapper.selectAll().stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return List.of();
        return permissionMapper.selectByRoleIds(roleIds).stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        if (roleId == null) return List.of();
        return permissionMapper.selectByRoleId(roleId).stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        permissionMapper.deleteByRoleId(roleId);
    }

    @Override
    public void saveBatch(Long roleId, List<Long> permissionIds) {
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissionMapper.insertBatch(roleId, permissionIds);
        }
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        return permissionMapper.selectIdsByRoleId(roleId);
    }
}
