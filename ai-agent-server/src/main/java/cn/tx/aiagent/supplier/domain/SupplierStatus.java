package cn.tx.aiagent.supplier.domain;

import lombok.Getter;

@Getter
public enum SupplierStatus {
    ACTIVE(1),
    DISABLED(0);

    private final int code;

    SupplierStatus(int code) {
        this.code = code;
    }

    public static SupplierStatus fromCode(int code) {
        return switch (code) {
            case 1 -> ACTIVE;
            case 0 -> DISABLED;
            default -> throw new IllegalArgumentException("Unknown supplier status code: " + code);
        };
    }
}
