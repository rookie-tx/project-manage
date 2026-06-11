package cn.tx.aiagent.user.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建用户请求 DTO。
 */
@Data
public class CreateUserRequest {

    @NotBlank
    @Size(max = 50)
    private String username;

    /** 可选：真实姓名 */
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /** 可选：邮箱 */
    @Size(max = 100)
    private String email;

    /** 可选：手机号 */
    @Size(max = 20)
    private String phone;

    /** 可选的角色 ID 列表 */
    private List<Long> roleIds;
}
