package cn.tx.aiagent.login.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名不能超过20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 20, message = "密码不能超过20个字符")
    private String password;

}
