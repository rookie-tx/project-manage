package cn.tx.aiagent.user.infrastructure;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户持久化对象，与 t_user 表字段一一对应（user 模块版本）。
 */
@Data
public class UserPO {

    private Long id;
    private String username;
    private String name;
    private String passwordHash;
    private String email;
    private String phone;
    private Integer status;
    private Integer mustChangePassword;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
