package cn.tx.aiagent.user.domain;

import lombok.Getter;

/**
 * 角色领域实体。
 * 使用私有构造器，通过静态工厂方法创建实例。
 */
@Getter
public class Role {

    private Long id;
    private String code;
    private String name;
    private String description;
    /** 0=可删除, 1=系统保护不可删除 */
    private Integer isProtected;

    private Role() {
    }

    public static Role fromPersistence(Long id, String code, String name, String description, int isProtected) {
        Role role = new Role();
        role.id = id;
        role.code = code;
        role.name = name;
        role.description = description;
        role.isProtected = isProtected;
        return role;
    }

    public boolean isProtected() {
        return isProtected != null && isProtected == 1;
    }
}
