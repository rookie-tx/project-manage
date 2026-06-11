package cn.tx.aiagent.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建角色请求 DTO。
 */
@Data
public class CreateRoleRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    /** 可选的权限 ID 列表 */
    private List<Long> permissionIds;
}
