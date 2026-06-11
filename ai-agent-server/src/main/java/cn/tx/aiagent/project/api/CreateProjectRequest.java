package cn.tx.aiagent.project.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** 创建项目请求 */
@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    @NotNull(message = "项目类型不能为空")
    private Integer type;

    private String location;
    private String clientName;
    private String supervisorUnit;

    @NotNull(message = "项目经理不能为空")
    private Long projectManagerId;

    @NotNull(message = "计划开工日期不能为空")
    private LocalDate plannedStartDate;

    @NotNull(message = "计划竣工日期不能为空")
    private LocalDate plannedEndDate;

    private String remark;
}
