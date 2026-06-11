package cn.tx.aiagent.warehouse.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StockRecordMapper {
    void insert(StockRecordPO po);
    StockRecordPO selectById(@Param("id") Long id);
    List<StockRecordPO> selectAll(@Param("warehouseId") Long warehouseId,
                                   @Param("materialId") Long materialId,
                                   @Param("type") Integer type,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);
    long count(@Param("warehouseId") Long warehouseId,
               @Param("materialId") Long materialId,
               @Param("type") Integer type);
}
