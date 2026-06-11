package cn.tx.aiagent.project.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目状态枚举，对应 t_project.status 字段。
 * 状态流转规则：
 * <pre>
 *   PLANNING  → IN_PROGRESS / SUSPENDED / COMPLETED
 *   IN_PROGRESS → SUSPENDED / COMPLETED
 *   SUSPENDED → IN_PROGRESS / COMPLETED
 *   COMPLETED → SETTLED
 *   SETTLED   → (不可变更)
 * </pre>
 */
@Getter
public enum ProjectStatus {

    PLANNING(0, "筹备中"),
    IN_PROGRESS(1, "在建"),
    SUSPENDED(2, "停工"),
    COMPLETED(3, "已竣工"),
    SETTLED(4, "已结算");

    private final int code;
    private final String description;

    ProjectStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean canTransitionTo(ProjectStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public static ProjectStatus fromCode(int code) {
        ProjectStatus status = CODE_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown project status code: " + code);
        }
        return status;
    }

    private static final Map<Integer, ProjectStatus> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ProjectStatus::getCode, s -> s));

    private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS = Map.of(
            PLANNING, EnumSet.of(IN_PROGRESS, COMPLETED, SUSPENDED),
            IN_PROGRESS, EnumSet.of(COMPLETED, SUSPENDED),
            SUSPENDED, EnumSet.of(IN_PROGRESS, COMPLETED),
            COMPLETED, EnumSet.of(SETTLED),
            SETTLED, EnumSet.noneOf(ProjectStatus.class)
    );
}
