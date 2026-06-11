package cn.tx.aiagent.task.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkflowTaskRepository {

    Optional<WorkflowTask> findById(String taskId);

    List<WorkflowTask> findByCandidateOrAssigned(String userId, int offset, int size);

    long countByCandidateOrAssigned(String userId);

    void complete(String taskId, Map<String, Object> variables);

    Map<String, Object> getVariables(String taskId, String... variableNames);

    Optional<WorkflowTask> findActiveByProcessInstanceId(String processInstanceId);
}
