package cn.tx.aiagent.supplier.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小程序供应商注册请求（公开接口，无需认证）。
 */
@Data
public class SupplierRegisterRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    private String licenseNo;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String address;
}
