package cn.tx.aiagent.user.infrastructure;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限持久化对象，与 t_permission 表字段一一对应。
 */
@Data
public class PermissionPO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
