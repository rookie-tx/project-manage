package cn.tx.aiagent.task.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.task.api.TaskResponse;
import cn.tx.aiagent.task.domain.BusinessTaskCallback;
import cn.tx.aiagent.task.domain.BusinessType;
import cn.tx.aiagent.task.domain.TaskResult;
import cn.tx.aiagent.task.domain.WorkflowInstance;
import cn.tx.aiagent.task.domain.WorkflowInstanceRepository;
import cn.tx.aiagent.task.domain.WorkflowTask;
import cn.tx.aiagent.task.domain.WorkflowTaskRepository;
import cn.tx.aiagent.task.infrastructure.CallbackRegistry;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskApplicationService {

    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final CallbackRegistry callbackRegistry;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public TaskApplicationService(WorkflowTaskRepository workflowTaskRepository,
                                   WorkflowInstanceRepository workflowInstanceRepository,
                                   CallbackRegistry callbackRegistry,
                                   RuntimeService runtimeService,
                                   HistoryService historyService) {
        this.workflowTaskRepository = workflowTaskRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.callbackRegistry = callbackRegistry;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    public void startProcess(BusinessType businessType, Long businessId,
                              Map<String, Object> variables) {
        workflowInstanceRepository.findByBusiness(businessType.name(), businessId)
                .ifPresent(__ -> {
                    throw new BusinessException(ErrorCode.TASK_PROCESS_ALREADY_STARTED);
                });

        BusinessTaskCallback callback = callbackRegistry.lookup(businessType);
        callback.validateBeforeStart(businessId, variables);

        var businessInfo = callback.getBusinessInfo(businessId);

        Map<String, Object> vars = new HashMap<>(variables);
        vars.put("businessType", businessType.name());
        vars.put("businessId", businessId);
        vars.put("businessTitle", businessInfo.title());
        vars.put("businessSubtitle", businessInfo.subtitle());

        try {
            ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                    businessType.getProcessDefinitionKey(), vars);
            WorkflowInstance wf = WorkflowInstance.create(
                    businessType.name(), businessId, pi.getId());
            workflowInstanceRepository.save(wf);
            callback.onProcessStarted(businessId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TASK_PROCESS_START_FAILED);
        }
    }

    public long countTasks(String assignee) {
        return workflowTaskRepository.countByCandidateOrAssigned(assignee);
    }

    public List<TaskResponse> listTasks(String assignee, int page, int size) {
        return workflowTaskRepository.findByCandidateOrAssigned(assignee, (page - 1) * size, size)
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    public void completeTask(String taskId, boolean approved, String comment) {
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        Long userId = LoginContextHolder.getUserId();
        String currentUser = userId != null ? String.valueOf(userId) : null;
        if (currentUser == null || !currentUser.equals(task.getAssignee())) {
            throw new BusinessException(ErrorCode.TASK_NOT_OWNER);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("approvalResult", approved ? "APPROVED" : "REJECTED");
        if (comment != null) {
            variables.put("comment", comment);
        }
        String assigneeName = LoginContextHolder.getName();
        if (assigneeName != null) {
            variables.put("assigneeName", assigneeName);
        }
        workflowTaskRepository.complete(taskId, variables);

        BusinessType businessType = BusinessType.valueOf(task.getBusinessType());
        BusinessTaskCallback callback = callbackRegistry.lookup(businessType);
        TaskResult result = approved ? new TaskResult.Approved() : new TaskResult.Rejected();

        workflowInstanceRepository.findByBusiness(task.getBusinessType(), task.getBusinessId())
                .ifPresent(wi -> {
                    boolean processEnded = workflowTaskRepository
                            .findActiveByProcessInstanceId(wi.getProcessInstanceId())
                            .isEmpty();
                    if (processEnded) {
                        // 流程所有节点完成后才回调业务，避免中间节点重复触发
                        callback.onTaskCompleted(task.getBusinessId(), result);
                        if (approved) {
                            wi.complete();
                        } else {
                            wi.terminate();
                        }
                        workflowInstanceRepository.updateStatus(wi.getId(), wi.getStatus());
                    }
                });
    }

    public String getCurrentApprovalNode(BusinessType businessType, Long businessId) {
        WorkflowInstance wi = workflowInstanceRepository
                .findByBusiness(businessType.name(), businessId)
                .orElse(null);
        if (wi == null || wi.getProcessInstanceId() == null) {
            return null;
        }
        return workflowTaskRepository.findActiveByProcessInstanceId(wi.getProcessInstanceId())
                .map(WorkflowTask::getNodeName)
                .orElse(null);
    }

    public List<HistoryResponse> getHistory(BusinessType businessType, Long businessId) {
        WorkflowInstance wi = workflowInstanceRepository
                .findByBusiness(businessType.name(), businessId)
                .orElse(null);

        if (wi == null || wi.getProcessInstanceId() == null) {
            return List.of();
        }

        List<HistoricTaskInstance> tasks = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(wi.getProcessInstanceId())
                .finished()
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime().asc()
                .list();

        return tasks.stream().map(t -> {
            String approvalResult = getTaskLocalVariable(t, "approvalResult");
            String result = "APPROVED".equals(approvalResult) ? "通过" : "驳回";
            String comment = getTaskLocalVariable(t, "comment");
            String assigneeName = getTaskLocalVariable(t, "assigneeName");
            return new HistoryResponse(
                    t.getName(),
                    t.getAssignee(),
                    assigneeName,
                    result,
                    comment,
                    t.getEndTime() != null
                            ? t.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : null
            );
        }).toList();
    }

    private TaskResponse toTaskResponse(WorkflowTask task) {
        return new TaskResponse(task.getTaskId(), task.getBusinessType(),
                task.getBusinessId(), task.getBusinessTitle(),
                task.getBusinessSubtitle(), task.getNodeName(),
                task.getCreateTime());
    }

    private static String getTaskLocalVariable(HistoricTaskInstance t, String name) {
        Map<String, Object> vars = t.getTaskLocalVariables();
        if (vars == null) return null;
        Object value = vars.get(name);
        return value instanceof String s ? s : null;
    }
}
