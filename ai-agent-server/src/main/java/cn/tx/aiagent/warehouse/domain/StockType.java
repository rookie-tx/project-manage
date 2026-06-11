package cn.tx.aiagent.warehouse.domain;

import lombok.Getter;

@Getter
public enum StockType {
    IN(1), OUT(2);

    private final int code;
    StockType(int code) { this.code = code; }

    public static StockType fromCode(int code) {
        return switch (code) {
            case 1 -> IN;
            case 2 -> OUT;
            default -> throw new IllegalArgumentException("Unknown stock type: " + code);
        };
    }
}
