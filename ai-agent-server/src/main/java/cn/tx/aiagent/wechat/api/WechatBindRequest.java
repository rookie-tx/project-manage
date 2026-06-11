package cn.tx.aiagent.wechat.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatBindRequest {
    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
