package cn.tx.aiagent.tempworker.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TempWorkerMapper {
    List<TempWorkerPO> selectByProjectId(@Param("projectId") Long projectId);
    TempWorkerPO selectById(@Param("id") Long id);
    TempWorkerPO selectByUserId(@Param("userId") Long userId);
    void insert(TempWorkerPO po);
    void update(TempWorkerPO po);
    void softDelete(@Param("id") Long id);
}
