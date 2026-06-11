package cn.tx.aiagent.project.api;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 项目响应，含项目经理用户名（由应用层查 UserRepository 填充） */
@Data
public class ProjectResponse {

    private Long id;
    private String code;
    private String name;
    private Integer type;
    private String location;
    private String clientName;
    private String supervisorUnit;
    private Long projectManagerId;
    private String projectManagerName;
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
