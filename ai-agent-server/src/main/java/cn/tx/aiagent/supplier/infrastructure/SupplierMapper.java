package cn.tx.aiagent.supplier.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplierMapper {
    List<SupplierPO> selectAll(@Param("keyword") String keyword,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("limit") int limit);
    long count(@Param("keyword") String keyword, @Param("status") Integer status);
    SupplierPO selectById(@Param("id") Long id);
    SupplierPO selectByUserId(@Param("userId") Long userId);
    void insert(SupplierPO po);
    void update(SupplierPO po);
    void softDelete(@Param("id") Long id);
}
