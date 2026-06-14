package cn.tx.aiagent.supplier.api;

import lombok.Data;

/**
 * 管理端更新供应商请求。
 */
@Data
public class UpdateSupplierRequest {

    private String companyName;

    private String licenseNo;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private String remark;

    private Integer status;
}
