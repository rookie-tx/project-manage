package cn.tx.aiagent.supplier.api;

import lombok.Data;

/**
 * 小程序供应商资料更新请求（仅联系人、电话、地址）。
 */
@Data
public class UpdateSupplierProfileRequest {

    private String contactPerson;

    private String contactPhone;

    private String address;
}
