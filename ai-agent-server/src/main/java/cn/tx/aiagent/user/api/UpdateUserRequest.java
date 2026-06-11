package cn.tx.aiagent.user.api;

import lombok.Data;

import java.util.List;

/**
 * 编辑用户请求 DTO。
 */
@Data
public class UpdateUserRequest {

    /** 可选：真实姓名 */
    private String name;

    /** 可选：邮箱 */
    private String email;

    /** 可选：手机号 */
    private String phone;

    /** 可选：1=ACTIVE, 0=DISABLED */
    private Integer status;

    /** 可选：角色 ID 列表（全量替换） */
    private List<Long> roleIds;
}
