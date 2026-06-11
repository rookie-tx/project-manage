package cn.tx.aiagent.project.domain;

import java.util.List;

/** 项目分页查询结果 */
public record ProjectListResult(
        List<Project> projects,
        long total,
        int page,
        int size
) {
}
