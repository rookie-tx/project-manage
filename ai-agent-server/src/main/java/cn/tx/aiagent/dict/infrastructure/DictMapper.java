package cn.tx.aiagent.dict.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictMapper {
    List<DictPO> selectByType(@Param("type") String type, @Param("enabled") Boolean enabled);
}
