package cn.tx.aiagent.warehouse.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StockBalanceMapper {
    StockBalancePO selectByWarehouseAndMaterial(@Param("warehouseId") Long warehouseId,
                                                  @Param("materialId") Long materialId);
    List<StockBalancePO> selectByWarehouseId(@Param("warehouseId") Long warehouseId);
    List<StockBalancePO> selectByMaterialId(@Param("materialId") Long materialId);
    void upsert(@Param("warehouseId") Long warehouseId, @Param("materialId") Long materialId,
                @Param("delta") double delta);
}
