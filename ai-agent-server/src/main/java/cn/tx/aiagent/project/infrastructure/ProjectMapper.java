package cn.tx.aiagent.project.infrastructure;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 项目 MyBatis Mapper，SQL 定义在 mapper/ProjectMapper.xml */
@Mapper
public interface ProjectMapper {

    void insert(ProjectPO po);

    ProjectPO selectById(@Param("id") Long id);

    ProjectPO selectByCode(@Param("code") String code);

    String selectMaxCodeByYear(@Param("year") int year);

    List<ProjectPO> selectAll(@Param("keyword") String keyword,
                              @Param("type") Integer type,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("size") int size);

    long countAll(@Param("keyword") String keyword,
                  @Param("type") Integer type,
                  @Param("status") Integer status);

    void update(ProjectPO po);

    void softDelete(@Param("id") Long id);

    List<ProjectPO> selectAllActive();

    /** 按项目统计物料采购汇总（仅已到货的采购单），GROUP BY 物料 */
    List<MaterialPurchaseStatsResult> selectMaterialPurchaseStats(@Param("projectId") Long projectId);

    /** 按名称模糊搜索项目，上限 10 条 */
    List<ProjectPO> selectByNameLike(@Param("keyword") String keyword);
}
