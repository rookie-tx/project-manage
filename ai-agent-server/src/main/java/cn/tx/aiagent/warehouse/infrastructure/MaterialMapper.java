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

    /** 按名称或规格模糊搜索物料，上限 10 条 */
    List<MaterialPO> selectByNameOrSpecLike(@Param("keyword") String keyword);
}
