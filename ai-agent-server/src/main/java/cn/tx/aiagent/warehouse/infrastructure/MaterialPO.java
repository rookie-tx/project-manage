package cn.tx.aiagent.warehouse.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialPO {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
