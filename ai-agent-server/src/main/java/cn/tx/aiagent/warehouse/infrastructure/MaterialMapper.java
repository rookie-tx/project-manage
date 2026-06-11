package cn.tx.aiagent.warehouse.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MaterialMapper {
    List<MaterialPO> selectAll();
    MaterialPO selectById(@Param("id") Long id);
    MaterialPO selectByCode(@Param("code") String code);
    void insert(MaterialPO po);
    void update(MaterialPO po);
    void softDelete(@Param("id") Long id);
}
