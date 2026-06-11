package cn.tx.aiagent.warehouse.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarehousePO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
