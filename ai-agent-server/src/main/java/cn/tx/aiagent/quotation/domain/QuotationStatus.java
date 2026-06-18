package cn.tx.aiagent.quotation.domain;

import lombok.Getter;

/**
 * 报价状态枚举
 */
@Getter
public enum QuotationStatus {

    PENDING(0, "待审核"),
    SELECTED(1, "已选用");

    private final int code;
    private final String label;

    QuotationStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QuotationStatus fromCode(int code) {
        for (QuotationStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知报价状态编码: " + code);
    }
}
