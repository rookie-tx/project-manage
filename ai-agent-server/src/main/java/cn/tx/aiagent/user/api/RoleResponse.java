package cn.tx.aiagent.user.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 角色响应 DTO。
 */
@Data
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private int isProtected;
}
