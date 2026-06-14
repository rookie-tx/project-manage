package cn.tx.aiagent.supplier.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端创建供应商请求。
 */
@Data
public class CreateSupplierRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    private String licenseNo;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String address;
}
