package cn.tx.aiagent.task.infrastructure;

import cn.tx.aiagent.task.application.TaskApplicationService;
import cn.tx.aiagent.task.domain.WorkflowInstanceRepository;
import cn.tx.aiagent.task.domain.WorkflowTaskRepository;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskDomainConfig {

    @Bean
    public TaskApplicationService taskApplicationService(WorkflowTaskRepository workflowTaskRepository,
                                                          WorkflowInstanceRepository workflowInstanceRepository,
                                                          CallbackRegistry callbackRegistry,
                                                          RuntimeService runtimeService,
                                                          HistoryService historyService) {
        return new TaskApplicationService(workflowTaskRepository, workflowInstanceRepository,
                callbackRegistry, runtimeService, historyService);
    }
}
