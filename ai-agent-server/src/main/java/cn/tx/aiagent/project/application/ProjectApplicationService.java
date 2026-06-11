package cn.tx.aiagent.project.application;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.user.domain.User;
import cn.tx.aiagent.user.domain.UserRepository;
import cn.tx.aiagent.project.api.ProjectResponse;
import cn.tx.aiagent.project.domain.*;
import org.springframework.stereotype.Service;

import java.time.Year;

/**
 * 项目应用服务，负责流程编排和异常转换。
 * 将领域层抛出的 IllegalArgumentException / IllegalStateException
 * 转为对应的 {@link cn.tx.aiagent.common.exception.BusinessException}。
 */
@Service
public class ProjectApplicationService {

    private final ProjectDomainService projectDomainService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectDtoMapper dtoMapper;

    public ProjectApplicationService(ProjectDomainService projectDomainService,
                                      ProjectRepository projectRepository,
                                      UserRepository userRepository,
                                      ProjectDtoMapper dtoMapper) {
        this.projectDomainService = projectDomainService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    /** 创建项目，将领域层 IllegalArgumentException 转为 PROJECT_MANAGER_NOT_FOUND */
    public Project create(Project project) {
        try {
            return projectDomainService.create(project);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PROJECT_MANAGER_NOT_FOUND);
        }
    }

    /**
     * 生成项目编号，格式 PRJ-{year}-{seq}，seq 为当年三位自增序号。
     * 查询当年最大编号后 +1，无记录时从 001 开始。
     * 数据库 UNIQUE 约束兜底并发冲突。
     */
    public String generateProjectCode() {
        int year = Year.now().getValue();
        String maxCode = projectRepository.findMaxCodeByYear(year).orElse(null);
        int nextSeq = 1;
        if (maxCode != null) {
            String seqPart = maxCode.substring(maxCode.lastIndexOf('-') + 1);
            nextSeq = Integer.parseInt(seqPart) + 1;
        }
        return String.format("PRJ-%d-%03d", year, nextSeq);
    }

    public Long getCurrentUserId() {
        return cn.tx.aiagent.common.config.LoginContextHolder.requireUserId();
    }

    public ProjectListResult list(ProjectSearchCriteria criteria) {
        return projectRepository.findAll(criteria);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    /** 更新项目，将领域层异常转为 PROJECT_MANAGER_NOT_FOUND 或 PROJECT_STATUS_TRANSITION_INVALID */
    public Project update(Long id, ProjectUpdateCommand command) {
        Project existing = getById(id);
        try {
            return projectDomainService.update(existing, command);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PROJECT_MANAGER_NOT_FOUND);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.PROJECT_STATUS_TRANSITION_INVALID);
        }
    }

    public void softDelete(Long id) {
        Project project = getById(id);
        projectDomainService.softDelete(project);
    }

    /**
     * 领域实体 → 响应 DTO。
     * 字段映射由 {@link ProjectDtoMapper} 完成，项目经理用户名需额外查库填充。
     */
    public ProjectResponse toResponse(Project project) {
        ProjectResponse resp = dtoMapper.toResponse(project);
        resp.setProjectManagerName(
                userRepository.findById(project.getProjectManagerId())
                        .map(u -> u.getName() != null ? u.getName() : u.getUsername())
                        .orElse(null));
        return resp;
    }
}
