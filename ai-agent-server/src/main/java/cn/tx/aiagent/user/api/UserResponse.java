package cn.tx.aiagent.user.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应 DTO。
 */
@Data
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;
    private LocalDateTime createdAt;
}
