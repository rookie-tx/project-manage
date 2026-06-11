package cn.tx.aiagent.task.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.security.RequirePermission;
import cn.tx.aiagent.task.application.HistoryResponse;
import cn.tx.aiagent.task.application.TaskApplicationService;
import cn.tx.aiagent.task.domain.BusinessType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @GetMapping("/count")
    public Result<Map<String, Long>> count() {
        Long userId = LoginContextHolder.getUserId();
        String assignee = userId != null ? String.valueOf(userId) : "0";
        return Result.success(Map.of("count", taskApplicationService.countTasks(assignee)));
    }

    @GetMapping
    public Result<List<TaskResponse>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Long userId = LoginContextHolder.getUserId();
        String assignee = userId != null ? String.valueOf(userId) : "0";
        return Result.success(taskApplicationService.listTasks(assignee, page, size));
    }

    @RequirePermission("task:complete")
    @PostMapping("/{taskId}/complete")
    public Result<Void> complete(@PathVariable String taskId,
                                  @RequestBody CompleteTaskRequest request) {
        taskApplicationService.completeTask(taskId, request.getApproved(), request.getComment());
        return Result.success();
    }

    @RequirePermission("pr:approve")
    @PostMapping("/start")
    public Result<Void> start(@RequestBody @Validated StartProcessRequest request) {
        BusinessType type = BusinessType.valueOf(request.getBusinessType());
        Map<String, Object> variables = request.getVariables() != null
                ? request.getVariables() : new HashMap<>();
        taskApplicationService.startProcess(type, request.getBusinessId(), variables);
        return Result.success();
    }

    @GetMapping("/history")
    public Result<List<HistoryResponse>> history(@RequestParam String businessType,
                                                   @RequestParam Long businessId) {
        BusinessType type = BusinessType.valueOf(businessType);
        return Result.success(taskApplicationService.getHistory(type, businessId));
    }
}
