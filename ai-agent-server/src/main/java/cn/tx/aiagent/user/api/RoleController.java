package cn.tx.aiagent.user.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.user.application.RoleApplicationService;
import cn.tx.aiagent.user.domain.Role;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 角色管理 Controller。
 */
@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleApplicationService roleApplicationService;

    public RoleController(RoleApplicationService roleApplicationService) {
        this.roleApplicationService = roleApplicationService;
    }

    /**
     * 查询所有角色列表。
     */
    @GetMapping("/roles")
    public Result<List<Role>> listRoles() {
        return Result.success(roleApplicationService.listRoles());
    }

    /**
     * 查询角色详情。
     */
    @GetMapping("/roles/{id}")
    public Result<Role> getRole(@PathVariable Long id) {
        return Result.success(roleApplicationService.getRole(id));
    }

    /**
     * 创建角色。
     */
    @RequirePermission("role:create")
    @PostMapping("/roles")
    public Result<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleApplicationService.createRole(
                request.getCode(), request.getName(), request.getDescription(), request.getPermissionIds());
        return Result.success(role);
    }

    /**
     * 编辑角色。
     */
    @RequirePermission("role:update")
    @PutMapping("/roles/{id}")
    public Result<Void> updateRole(@PathVariable Long id,
                                    @Valid @RequestBody UpdateRoleRequest request) {
        roleApplicationService.updateRole(id, request.getName(), request.getDescription(), request.getPermissionIds());
        return Result.success();
    }

    /**
     * 删除角色（受保护角色不可删除）。
     */
    @RequirePermission("role:delete")
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleApplicationService.deleteRole(id);
        return Result.success();
    }

    /**
     * 查询角色关联的权限 ID 列表。
     */
    @RequirePermission("role:permission:assign")
    @GetMapping("/roles/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleApplicationService.getRolePermissionIds(id));
    }
}
