package cn.tx.aiagent.project.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目类型枚举，对应 t_project.type 字段。
 * 使用 SMALLINT 编码存储，{@link #fromCode(int)} 反查。
 */
@Getter
public enum ProjectType {

    GENERAL_CONSTRUCTION(0, "房建"),
    MUNICIPAL(1, "市政"),
    RENOVATION(2, "装修"),
    ROAD_BRIDGE(3, "路桥"),
    WATER_CONSERVANCY(4, "水利"),
    OTHER(9, "其他");

    private final int code;
    private final String description;

    ProjectType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProjectType fromCode(int code) {
        ProjectType type = CODE_MAP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("Unknown project type code: " + code);
        }
        return type;
    }

    private static final Map<Integer, ProjectType> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ProjectType::getCode, t -> t));
}
