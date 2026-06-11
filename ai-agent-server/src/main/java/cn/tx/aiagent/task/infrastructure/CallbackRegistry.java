package cn.tx.aiagent.task.infrastructure;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.task.domain.BusinessTaskCallback;
import cn.tx.aiagent.task.domain.BusinessType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CallbackRegistry {

    private final Map<BusinessType, BusinessTaskCallback> registry;

    public CallbackRegistry(List<BusinessTaskCallback> callbacks) {
        this.registry = callbacks.stream()
                .collect(Collectors.toMap(BusinessTaskCallback::getType, c -> c));
    }

    public BusinessTaskCallback lookup(BusinessType type) {
        return Optional.ofNullable(registry.get(type))
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_CALLBACK_NOT_FOUND));
    }
}
