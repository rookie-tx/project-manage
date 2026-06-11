package cn.tx.aiagent.project.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目领域实体，聚合根。
 * 使用私有构造器，通过静态工厂方法创建实例：
 * <ul>
 *   <li>{@link #create} — 新建项目，初始状态为 {@link ProjectStatus#PLANNING}</li>
 *   <li>{@link #fromPersistence} — 从持久层还原</li>
 * </ul>
 */
@Getter
public class Project {

    private Long id;
    private String code;
    private String name;
    private ProjectType type;
    private String location;
    private String clientName;
    private String supervisorUnit;
    private Long projectManagerId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private String remark;
    /** 0=正常, 1=已删除 */
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Project() {
    }

    /** 创建新项目，状态初始化为 PLANNING，isDeleted 置 0 */
    public static Project create(String code, String name, ProjectType type, String location,
                                  String clientName, String supervisorUnit, Long projectManagerId,
                                  LocalDate plannedStartDate, LocalDate plannedEndDate,
                                  String remark, Long createdBy) {
        Project project = new Project();
        project.code = code;
        project.name = name;
        project.type = type;
        project.location = location;
        project.clientName = clientName;
        project.supervisorUnit = supervisorUnit;
        project.projectManagerId = projectManagerId;
        project.plannedStartDate = plannedStartDate;
        project.plannedEndDate = plannedEndDate;
        project.status = ProjectStatus.PLANNING;
        project.remark = remark;
        project.isDeleted = 0;
        project.createdBy = createdBy;
        return project;
    }

    /** 从持久层数据还原领域实体，将 SMALLINT 编码转为枚举 */
    public static Project fromPersistence(Long id, String code, String name, int typeCode,
                                           String location, String clientName, String supervisorUnit,
                                           Long projectManagerId, LocalDate plannedStartDate,
                                           LocalDate plannedEndDate, LocalDate actualStartDate,
                                           LocalDate actualEndDate, int statusCode, String remark,
                                           int isDeleted, Long createdBy,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        Project project = new Project();
        project.id = id;
        project.code = code;
        project.name = name;
        project.type = ProjectType.fromCode(typeCode);
        project.location = location;
        project.clientName = clientName;
        project.supervisorUnit = supervisorUnit;
        project.projectManagerId = projectManagerId;
        project.plannedStartDate = plannedStartDate;
        project.plannedEndDate = plannedEndDate;
        project.actualStartDate = actualStartDate;
        project.actualEndDate = actualEndDate;
        project.status = ProjectStatus.fromCode(statusCode);
        project.remark = remark;
        project.isDeleted = isDeleted;
        project.createdBy = createdBy;
        project.createdAt = createdAt;
        project.updatedAt = updatedAt;
        return project;
    }

    /** 状态流转，非法流转抛出 {@link IllegalStateException}，由应用层转为 BusinessException */
    public void transitionTo(ProjectStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException(
                    "Cannot transition from " + status + " to " + target);
        }
        this.status = target;
    }

    /** 软删除标记 */
    public void markDeleted() {
        this.isDeleted = 1;
    }

    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }

    /**
     * 部分更新：仅替换 command 中非 null 的字段，null 字段保持原值。
     * 状态变更不在此处理，由 {@link ProjectDomainService#update} 统一执行 {@link #transitionTo}。
     */
    public void update(ProjectUpdateCommand cmd) {
        this.name = cmd.name();
        this.type = cmd.typeCode() != null ? ProjectType.fromCode(cmd.typeCode()) : this.type;
        this.location = cmd.location();
        this.clientName = cmd.clientName();
        this.supervisorUnit = cmd.supervisorUnit();
        this.projectManagerId = cmd.projectManagerId() != null ? cmd.projectManagerId() : this.projectManagerId;
        this.plannedStartDate = cmd.plannedStartDate() != null ? cmd.plannedStartDate() : this.plannedStartDate;
        this.plannedEndDate = cmd.plannedEndDate() != null ? cmd.plannedEndDate() : this.plannedEndDate;
        this.actualStartDate = cmd.actualStartDate() != null ? cmd.actualStartDate() : this.actualStartDate;
        this.actualEndDate = cmd.actualEndDate() != null ? cmd.actualEndDate() : this.actualEndDate;
        this.remark = cmd.remark();
    }
}
