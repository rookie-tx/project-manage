package cn.tx.aiagent.task.domain;

import java.util.Map;

public interface BusinessTaskCallback {

    BusinessType getType();

    BusinessInfo getBusinessInfo(Long businessId);

    default void validateBeforeStart(Long businessId, Map<String, Object> variables) {}

    default void onProcessStarted(Long businessId) {}

    void onTaskCompleted(Long businessId, TaskResult result);
}
