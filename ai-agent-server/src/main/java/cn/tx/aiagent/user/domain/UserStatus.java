package cn.tx.aiagent.user.domain;

import lombok.Getter;

@Getter
public enum UserStatus {

    ACTIVE(1),

    DISABLED(0);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public static UserStatus fromCode(int code) {
        return switch (code) {
            case 1 -> ACTIVE;
            case 0 -> DISABLED;
            default -> throw new IllegalArgumentException("Unknown status code: " + code);
        };
    }
}
