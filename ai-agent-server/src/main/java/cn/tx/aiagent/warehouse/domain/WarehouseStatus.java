package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;

@Getter
public enum WarehouseStatus {
    ACTIVE(1), DISABLED(0);

    private final int code;
    WarehouseStatus(int code) { this.code = code; }

    public static WarehouseStatus fromCode(int code) {
        return switch (code) {
            case 1 -> ACTIVE;
            case 0 -> DISABLED;
            default -> throw new IllegalArgumentException("Unknown warehouse status: " + code);
        };
    }
}
