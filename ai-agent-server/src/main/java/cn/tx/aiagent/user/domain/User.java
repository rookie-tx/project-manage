package cn.tx.aiagent.user.domain;

// UserStatus 已合并到 user 模块
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户管理领域实体（user 模块版本）。
 * 相比 login 模块的 User，增加了 roles 集合和 isDeleted 字段。
 * 使用私有构造器，通过静态工厂方法创建实例。
 */
@Getter
public class User {

    private Long id;
    private String username;
    private String name;
    private String passwordHash;
    private String email;
    private String phone;
    private UserStatus status;
    private Integer mustChangePassword;
    /** 0=正常, 1=已删除 */
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private List<Role> roles;

    private User() {
    }

    /** 创建新用户 */
    public static User create(String username, String name, String passwordHash, String email, String phone) {
        User user = new User();
        user.username = username;
        user.name = name;
        user.passwordHash = passwordHash;
        user.email = email;
        user.phone = phone;
        user.status = UserStatus.ACTIVE;
        user.mustChangePassword = 0;
        user.isDeleted = 0;
        user.roles = new ArrayList<>();
        return user;
    }

    /** 从持久层还原领域实体 */
    public static User fromPersistence(Long id, String username, String name, String passwordHash,
                                       String email, String phone,
                                       int statusCode, int mustChangePassword, int isDeleted,
                                       LocalDateTime createdAt) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.name = name;
        user.passwordHash = passwordHash;
        user.email = email;
        user.phone = phone;
        user.status = UserStatus.fromCode(statusCode);
        user.mustChangePassword = mustChangePassword;
        user.isDeleted = isDeleted;
        user.createdAt = createdAt;
        user.roles = new ArrayList<>();
        return user;
    }

    /** 兼容旧签名（无 name 和 mustChangePassword，默认 null/0） */
    public static User fromPersistence(Long id, String username, String passwordHash,
                                       String email, String phone,
                                       int statusCode, int isDeleted,
                                       LocalDateTime createdAt) {
        return fromPersistence(id, username, null, passwordHash, email, phone, statusCode, 0, isDeleted, createdAt);
    }

    /** 设置角色列表 */
    public void setRoles(List<Role> roles) {
        this.roles = roles != null ? roles : new ArrayList<>();
    }

    /** 获取角色名称列表 */
    public List<String> getRoleNames() {
        if (roles == null) return Collections.emptyList();
        return roles.stream().map(Role::getName).toList();
    }

    /** 软删除标记 */
    public void markDeleted() {
        this.isDeleted = 1;
    }

    /** 是否已删除 */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }

    /**
     * 从持久层还原领域实体（兼容旧签名：直接传 UserStatus 枚举）。
     */
    public static User fromPersistence(Long id, String username, String passwordHash,
                                       UserStatus status, java.time.LocalDateTime createdAt) {
        return fromPersistence(id, username, null, passwordHash, null, null, status.getCode(), 0, 0, createdAt);
    }

    /** 是否活跃 */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /** 更新状态 */
    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    /** 更新密码哈希 */
    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** 更新基本信息 */
    public void updateProfile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public void passwordChanged() { this.mustChangePassword = 0; }

    public boolean mustChangePassword() { return mustChangePassword != null && mustChangePassword == 1; }
}
