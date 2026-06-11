package cn.tx.aiagent.warehouse.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMaterialRequest {
    @NotBlank @Size(max = 50)
    private String code;
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 100)
    private String spec;
    @Size(max = 20)
    private String unit;
}
