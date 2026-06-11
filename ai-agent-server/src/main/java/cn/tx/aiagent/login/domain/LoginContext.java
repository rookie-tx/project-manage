package cn.tx.aiagent.login.domain;

import java.util.Collections;
import java.util.List;

public record LoginContext(long userId, String username, String name, List<String> roles, List<String> permissions,
                            boolean mustChangePassword) {

    public LoginContext(long userId, String username, String name) {
        this(userId, username, name, Collections.emptyList(), Collections.emptyList(), false);
    }

    public LoginContext(long userId, String username) {
        this(userId, username, null, Collections.emptyList(), Collections.emptyList(), false);
    }
}
