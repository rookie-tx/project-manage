package cn.tx.aiagent.common.util;

/**
 * 地理位置工具类，提供 Haversine 距离计算和模拟定位风险评估。
 *
 * <p>坐标系约定：微信小程序返回 GCJ-02 坐标，本工具类计算均基于 GCJ-02。</p>
 */
public final class LocationUtils {

    /** 地球平均半径（米） */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    /** 精度阈值：超过此值标记为可疑（米） */
    private static final double ACCURACY_THRESHOLD = 50.0;

    /** 打卡时允许的速度上限（m/s），>0 表示移动中 */
    private static final double SPEED_THRESHOLD = 0.0;

    /** 位置时间戳与服务器时间的最大允许偏差（秒） */
    private static final long TIMESTAMP_DRIFT_THRESHOLD_SEC = 300;

    private LocationUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 使用 Haversine 公式计算两点间距离（米）。
     *
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 两点间距离，单位为米
     */
    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    /**
     * 评估定位风险等级。
     *
     * <p>检测维度：
     * <ul>
     *   <li>精度异常：accuracy &gt; 50m</li>
     *   <li>垂直精度异常：verticalAccuracy &lt; 0（模拟器特征）</li>
     *   <li>速度异常：speed &gt; 0（打卡时不应移动）</li>
     *   <li>海拔异常：altitude == 0（伪造特征）</li>
     *   <li>时间戳偏差：与服务器时间差 &gt; 5 分钟</li>
     * </ul>
     *
     * @param accuracy          水平定位精度（米），-1 表示未知
     * @param verticalAccuracy  垂直定位精度（米），-1 表示未知
     * @param speed             速度（m/s），-1 表示未知
     * @param altitude          海拔（米），0 或未知时为 0
     * @param locationTimestamp 位置获取时间戳（毫秒），0 表示未知
     * @param serverTimestamp   服务器当前时间戳（毫秒）
     * @return 风险等级：0=正常, 1=可疑, 2=高风险, 3=严重
     */
    public static int assessLocationRisk(double accuracy, double verticalAccuracy, double speed,
                                         double altitude, long locationTimestamp, long serverTimestamp) {
        int risk = 0;

        // 精度异常 → +1
        if (accuracy > ACCURACY_THRESHOLD) {
            risk += 1;
        }

        // 垂直精度为负 → 模拟器特征 → +2
        if (verticalAccuracy < 0 && verticalAccuracy != -1) {
            risk += 2;
        }

        // 打卡时有移动速度 → +1
        if (speed > SPEED_THRESHOLD) {
            risk += 1;
        }

        // 海拔恰好为 0（典型伪造数据） → +1
        if (altitude == 0.0 && verticalAccuracy >= 0) {
            risk += 1;
        }

        // 时间戳偏差超过5分钟 → +1
        if (locationTimestamp > 0 && serverTimestamp > 0) {
            long driftSec = Math.abs(serverTimestamp - locationTimestamp) / 1000;
            if (driftSec > TIMESTAMP_DRIFT_THRESHOLD_SEC) {
                risk += 1;
            }
        }

        return Math.min(risk, 3);
    }
}
