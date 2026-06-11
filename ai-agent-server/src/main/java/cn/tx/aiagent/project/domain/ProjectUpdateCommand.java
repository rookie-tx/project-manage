package cn.tx.aiagent.project.domain;

import java.time.LocalDate;

/** 项目更新命令，所有字段可选，null 表示不更新该字段 */
public record ProjectUpdateCommand(
        String name,
        Integer typeCode,
        String location,
        String clientName,
        String supervisorUnit,
        Long projectManagerId,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate actualStartDate,
        LocalDate actualEndDate,
        Integer statusCode,
        String remark
) {
}
