package cn.tx.aiagent.task.infrastructure;

import cn.tx.aiagent.task.domain.WorkflowTask;
import cn.tx.aiagent.task.domain.WorkflowTaskRepository;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FlowableWorkflowTaskRepository implements WorkflowTaskRepository {

    private final TaskService taskService;

    public FlowableWorkflowTaskRepository(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Optional<WorkflowTask> findById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return Optional.empty();
        return Optional.of(toWorkflowTask(task));
    }

    @Override
    public List<WorkflowTask> findByCandidateOrAssigned(String userId, int offset, int size) {
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .orderByTaskCreateTime().desc()
                .listPage(offset, size)
                .stream()
                .map(this::toWorkflowTask)
                .toList();
    }

    @Override
    public long countByCandidateOrAssigned(String userId) {
        return taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .count();
    }

    @Override
    public void complete(String taskId, Map<String, Object> variables) {
        taskService.setVariablesLocal(taskId, variables);
        taskService.complete(taskId, variables);
    }

    @Override
    public Map<String, Object> getVariables(String taskId, String... variableNames) {
        return taskService.getVariables(taskId, List.of(variableNames));
    }

    @Override
    public Optional<WorkflowTask> findActiveByProcessInstanceId(String processInstanceId) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (task == null) return Optional.empty();
        return Optional.of(toWorkflowTask(task));
    }

    private WorkflowTask toWorkflowTask(Task task) {
        Map<String, Object> vars = taskService.getVariables(task.getId(),
                List.of("businessType", "businessId", "businessTitle", "businessSubtitle"));

        String businessType = vars.get("businessType") instanceof String s ? s : null;
        Long businessId = null;
        Object bidObj = vars.get("businessId");
        if (bidObj instanceof Number n) {
            businessId = n.longValue();
        }

        String businessTitle = vars.get("businessTitle") instanceof String s ? s : "";
        String businessSubtitle = vars.get("businessSubtitle") instanceof String s ? s : "";

        LocalDateTime createTime = task.getCreateTime() != null
                ? task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;

        return WorkflowTask.fromPersistence(task.getId(), task.getName(),
                task.getAssignee(), businessType, businessId, businessTitle,
                businessSubtitle, createTime);
    }
}
