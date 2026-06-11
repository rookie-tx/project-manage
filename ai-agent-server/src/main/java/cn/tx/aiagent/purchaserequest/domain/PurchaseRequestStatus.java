package cn.tx.aiagent.purchaserequest.domain;

import lombok.Getter;

@Getter
public enum PurchaseRequestStatus {

    PENDING(0, "待提交"),
    APPROVING(1, "审批中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回"),
    CANCELLED(4, "已取消");

    private final int code;

    private final String label;

    PurchaseRequestStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PurchaseRequestStatus fromCode(int code) {
        for (PurchaseRequestStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知采购申请状态编码: " + code);
    }
}
