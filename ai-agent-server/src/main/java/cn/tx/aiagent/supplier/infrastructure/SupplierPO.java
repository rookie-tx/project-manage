package cn.tx.aiagent.supplier.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierPO {
    private Long id;
    private Long userId;
    private String companyName;
    private String licenseNo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
