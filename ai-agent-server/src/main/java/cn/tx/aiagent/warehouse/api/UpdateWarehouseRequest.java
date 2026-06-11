package cn.tx.aiagent.warehouse.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWarehouseRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 200)
    private String location;
    private Integer status;
}
