package cn.tx.aiagent.task.infrastructure;

import cn.tx.aiagent.task.domain.WorkflowInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkflowInstancePoMapper {

    default WorkflowInstance toDomain(WorkflowInstancePO po) {
        if (po == null) return null;
        return WorkflowInstance.fromPersistence(
                po.getId(), po.getBusinessType(), po.getBusinessId(),
                po.getProcessInstanceId(), po.getStatus(),
                po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WorkflowInstancePO toPO(WorkflowInstance instance);
}
