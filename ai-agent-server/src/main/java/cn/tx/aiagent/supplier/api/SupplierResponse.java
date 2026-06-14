package cn.tx.aiagent.supplier.api;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商响应 DTO。
 */
@Data
public class SupplierResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String licenseNo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
