package cn.tx.aiagent.constructionwaste.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ConstructionWasteMapper {
    void insert(ConstructionWastePO po);
    ConstructionWastePO selectById(@Param("id") Long id);
    List<ConstructionWastePO> search(@Param("projectId") Long projectId,
                                      @Param("wasteType") String wasteType,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);
    long count(@Param("projectId") Long projectId,
               @Param("wasteType") String wasteType,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate);
    void softDelete(@Param("id") Long id);
}
