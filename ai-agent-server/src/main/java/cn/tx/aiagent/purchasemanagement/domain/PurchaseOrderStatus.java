package cn.tx.aiagent.purchasemanagement.domain;

import lombok.Getter;

@Getter
public enum PurchaseOrderStatus {

    PURCHASING(0, "采购中"),
    PURCHASED(1, "已采购"),
    ARRIVED(2, "已到货");

    private final int code;
    private final String label;

    PurchaseOrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public boolean canTransitionTo(PurchaseOrderStatus target) {
        return this == PURCHASING && target == PURCHASED
            || this == PURCHASED && target == ARRIVED;
    }

    public static PurchaseOrderStatus fromCode(int code) {
        for (PurchaseOrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知采购状态编码: " + code);
    }
}
