package cn.tx.aiagent.project.infrastructure;

import cn.tx.aiagent.project.domain.Project;
import cn.tx.aiagent.project.domain.ProjectListResult;
import cn.tx.aiagent.project.domain.ProjectRepository;
import cn.tx.aiagent.project.domain.ProjectSearchCriteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 项目仓储实现，PO ↔ Domain 映射委托给 {@link ProjectPoMapper}。
 */
@Component
public class ProjectRepositoryImpl implements ProjectRepository {

    private final ProjectMapper projectMapper;
    private final ProjectPoMapper poMapper;

    public ProjectRepositoryImpl(ProjectMapper projectMapper, ProjectPoMapper poMapper) {
        this.projectMapper = projectMapper;
        this.poMapper = poMapper;
    }

    @Override
    public void save(Project project) {
        projectMapper.insert(poMapper.toPO(project));
    }

    @Override
    public Optional<Project> findById(Long id) {
        ProjectPO po = projectMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<Project> findByCode(String code) {
        ProjectPO po = projectMapper.selectByCode(code);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<String> findMaxCodeByYear(int year) {
        return Optional.ofNullable(projectMapper.selectMaxCodeByYear(year));
    }

    @Override
    public ProjectListResult findAll(ProjectSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<ProjectPO> poList = projectMapper.selectAll(
                criteria.keyword(), criteria.typeCode(), criteria.statusCode(),
                offset, criteria.size());
        long total = projectMapper.countAll(
                criteria.keyword(), criteria.typeCode(), criteria.statusCode());
        List<Project> projects = poList.stream().map(poMapper::toDomain).toList();
        return new ProjectListResult(projects, total, criteria.page(), criteria.size());
    }

    @Override
    public void update(Project project) {
        projectMapper.update(poMapper.toPO(project));
    }

    @Override
    public List<Project> findAllActive() {
        return projectMapper.selectAllActive().stream()
                .map(poMapper::toDomain)
                .toList();
    }

    @Override
    public List<Project> findByNameLike(String keyword) {
        return projectMapper.selectByNameLike(keyword).stream()
                .map(poMapper::toDomain)
                .toList();
    }
}
