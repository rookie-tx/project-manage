package cn.tx.aiagent.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑角色请求 DTO。
 */
@Data
public class UpdateRoleRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    /** 权限 ID 列表（全量替换） */
    private List<Long> permissionIds;
}
