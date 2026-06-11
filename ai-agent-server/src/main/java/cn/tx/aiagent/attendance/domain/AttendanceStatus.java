package cn.tx.aiagent.attendance.domain;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    CLOCKED_IN(1), CLOCKED_OUT(2);
    private final int code;
    AttendanceStatus(int code) { this.code = code; }
    public static AttendanceStatus fromCode(int code) {
        return switch (code) { case 1 -> CLOCKED_IN; case 2 -> CLOCKED_OUT; default -> throw new IllegalArgumentException("Unknown status: " + code); };
    }
}
