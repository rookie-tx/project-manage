package cn.tx.aiagent.warehouse.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WarehouseMapper {
    List<WarehousePO> selectAll();
    WarehousePO selectById(@Param("id") Long id);
    WarehousePO selectByCode(@Param("code") String code);
    void insert(WarehousePO po);
    void update(WarehousePO po);
    void softDelete(@Param("id") Long id);
}
