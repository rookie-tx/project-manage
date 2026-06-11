package cn.tx.aiagent.user.infrastructure;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色持久化对象，与 t_role 表字段一一对应。
 */
@Data
public class RolePO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer isProtected;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
