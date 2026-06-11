package cn.tx.aiagent.task.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkflowInstanceMapper {

    void insert(WorkflowInstancePO po);

    WorkflowInstancePO selectByBusiness(@Param("businessType") String businessType,
                                         @Param("businessId") Long businessId);

    WorkflowInstancePO selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
