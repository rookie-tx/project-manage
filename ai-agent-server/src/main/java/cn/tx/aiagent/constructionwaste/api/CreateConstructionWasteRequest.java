package cn.tx.aiagent.constructionwaste.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateConstructionWasteRequest {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "废渣种类不能为空")
    private String wasteType;

    @NotNull(message = "数量不能为空")
    private Double quantity;

    @NotBlank(message = "单位不能为空")
    private String unit;

    private String vehiclePlate;
    private String driverName;
    private String destination;

    @NotNull(message = "运出日期不能为空")
    private LocalDate removalDate;

    private String remark;

    @NotNull(message = "照片不能为空")
    @Size(min = 1, message = "至少需要上传一张照片")
    private List<Long> fileIds;
}
