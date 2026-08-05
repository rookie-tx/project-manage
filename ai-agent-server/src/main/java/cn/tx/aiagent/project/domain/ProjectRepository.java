package cn.tx.aiagent.project.domain;

import java.util.List;
import java.util.Optional;

/** 项目仓储接口，定义领域层所需的数据访问能力 */
public interface ProjectRepository {

    /** 新增项目 */
    void save(Project project);

    /** 按主键查询，排除已删除 */
    Optional<Project> findById(Long id);

    /** 按项目编号查询，排除已删除 */
    Optional<Project> findByCode(String code);

    /** 查询指定年份的最大项目编号，用于生成新编号 */
    Optional<String> findMaxCodeByYear(int year);

    /** 分页/筛选查询 */
    ProjectListResult findAll(ProjectSearchCriteria criteria);

    /** 更新项目全部字段 */
    void update(Project project);

    /** 查询所有未删除的项目，供下拉选择 */
    List<Project> findAllActive();

    /** 按名称模糊搜索，上限 10 条，供 Agent 工具调用 */
    List<Project> findByNameLike(String keyword);
}
