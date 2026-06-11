package cn.tx.aiagent.tempworker.domain;

import lombok.Getter;

@Getter
public enum WorkerStatus {
    ACTIVE(1), LEFT(0);

    private final int code;
    WorkerStatus(int code) { this.code = code; }

    public static WorkerStatus fromCode(int code) {
        return switch (code) { case 1 -> ACTIVE; case 0 -> LEFT; default -> throw new IllegalArgumentException("Unknown status: " + code); };
    }
}
