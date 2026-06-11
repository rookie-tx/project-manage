package cn.tx.aiagent.project.infrastructure;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 项目持久化对象，与 t_project 表字段一一对应 */
@Data
public class ProjectPO {

    private Long id;
    private String code;
    private String name;
    private Integer type;
    private String location;
    private String clientName;
    private String supervisorUnit;
    private Long projectManagerId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Integer status;
    private String remark;
    private Integer isDeleted;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
