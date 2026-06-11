package cn.tx.aiagent.user.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.user.application.UserApplicationService;
import cn.tx.aiagent.user.application.UserDtoMapper;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserListResult;
import cn.tx.aiagent.user.domain.UserRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 用户管理 Controller（user 模块）。
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserDtoMapper userDtoMapper;
    private final UserRepository userRepository;

    public UserController(UserApplicationService userApplicationService,
                           UserDtoMapper userDtoMapper,
                           UserRepository userRepository) {
        this.userApplicationService = userApplicationService;
        this.userDtoMapper = userDtoMapper;
        this.userRepository = userRepository;
    }

    /**
     * 查询活跃用户列表（下拉选择用，不分页）。
     */
    @GetMapping("/users/select")
    public Result<List<UserResponse>> listActiveUsers() {
        List<UserResponse> users = userRepository.findAllActive().stream()
                .map(userDtoMapper::toResponse)
                .toList();
        return Result.success(users);
    }

    /**
     * 分页查询用户列表。
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserListResult result = userApplicationService.listUsers(keyword, status, page, size);
        List<UserResponse> records = result.records().stream()
                .map(userDtoMapper::toResponse)
                .toList();
        return Result.success(Map.of(
                "records", records,
                "total", result.total(),
                "page", result.page(),
                "size", result.size()
        ));
    }

    /**
     * 查询用户详情。
     */
    @GetMapping("/users/{id}")
    public Result<UserResponse> getUserDetail(@PathVariable Long id) {
        User user = userApplicationService.getUserDetail(id);
        return Result.success(userDtoMapper.toResponse(user));
    }

    /**
     * 创建用户。
     */
    @RequirePermission("user:create")
    @PostMapping("/users")
    public Result<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userApplicationService.createUser(
                request.getUsername(), request.getName(), request.getPassword(), request.getEmail(), request.getPhone(), request.getRoleIds());
        return Result.success(userDtoMapper.toResponse(user));
    }

    /**
     * 编辑用户。
     */
    @RequirePermission("user:update")
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                    @RequestBody UpdateUserRequest request) {
        userApplicationService.updateUser(id, request.getName(), request.getEmail(), request.getPhone(), request.getStatus(), request.getRoleIds());
        return Result.success();
    }

    /**
     * 软删除用户。
     */
    @RequirePermission("user:delete")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return Result.success();
    }
}
