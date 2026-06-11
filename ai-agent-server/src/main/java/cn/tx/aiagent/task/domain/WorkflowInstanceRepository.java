package cn.tx.aiagent.task.domain;

import java.util.Optional;

public interface WorkflowInstanceRepository {

    void save(WorkflowInstance instance);

    Optional<WorkflowInstance> findByBusiness(String businessType, Long businessId);

    Optional<WorkflowInstance> findByProcessInstanceId(String processInstanceId);

    void updateStatus(Long id, Integer status);
}
