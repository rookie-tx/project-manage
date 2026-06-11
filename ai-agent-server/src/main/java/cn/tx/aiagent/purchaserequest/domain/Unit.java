package cn.tx.aiagent.purchaserequest.domain;

public enum Unit {

    PIECE(0, "个"),
    UNIT(1, "台"),
    SET(2, "套"),
    METER(3, "米"),
    TON(4, "吨"),
    CUBIC_METER(5, "立方米"),
    ROLL(6, "卷"),
    BUCKET(7, "桶"),
    BOX(8, "箱"),
    OTHER(9, "其他");

    private final int code;
    private final String label;

    Unit(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }

    public String getLabel() { return label; }

    public static Unit fromCode(int code) {
        for (Unit u : values()) {
            if (u.code == code) return u;
        }
        throw new IllegalArgumentException("未知单位编码: " + code);
    }
}
