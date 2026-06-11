package cn.tx.aiagent.login.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponse {

    private long userId;

    private String username;

    private String name;

    private List<String> roles;

    private List<String> permissions;

    private boolean mustChangePassword;

    public LoginResponse(long userId, String username, String name) {
        this.userId = userId;
        this.username = username;
        this.name = name;
    }

    public LoginResponse(long userId, String username, List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
    }

}
