package cn.tx.aiagent.task.infrastructure;

import cn.tx.aiagent.task.domain.WorkflowInstance;
import cn.tx.aiagent.task.domain.WorkflowInstanceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkflowInstanceRepositoryImpl implements WorkflowInstanceRepository {

    private final WorkflowInstanceMapper mapper;
    private final WorkflowInstancePoMapper poMapper;

    public WorkflowInstanceRepositoryImpl(WorkflowInstanceMapper mapper,
                                           WorkflowInstancePoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public void save(WorkflowInstance instance) {
        mapper.insert(poMapper.toPO(instance));
    }

    @Override
    public Optional<WorkflowInstance> findByBusiness(String businessType, Long businessId) {
        WorkflowInstancePO po = mapper.selectByBusiness(businessType, businessId);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<WorkflowInstance> findByProcessInstanceId(String processInstanceId) {
        WorkflowInstancePO po = mapper.selectByProcessInstanceId(processInstanceId);
        if (po == null) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        mapper.updateStatus(id, status);
    }
}
