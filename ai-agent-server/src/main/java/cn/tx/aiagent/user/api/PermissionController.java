package cn.tx.aiagent.user.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.user.domain.Permission;
import cn.tx.aiagent.user.domain.PermissionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限查询 Controller。
 */
@RestController
@RequestMapping("/api")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * 查询所有权限列表。
     */
    @GetMapping("/permissions")
    public Result<List<Permission>> listPermissions() {
        return Result.success(permissionRepository.findAll());
    }
}
