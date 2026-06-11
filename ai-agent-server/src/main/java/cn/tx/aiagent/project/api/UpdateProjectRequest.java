package cn.tx.aiagent.project.api;

import lombok.Data;

import java.time.LocalDate;

/** 更新项目请求，所有字段可选 */
@Data
public class UpdateProjectRequest {

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
}
