package cn.tx.aiagent.user.domain;

import lombok.Getter;

/**
 * 权限领域实体。
 * 使用私有构造器，通过静态工厂方法创建实例。
 */
@Getter
public class Permission {

    private Long id;
    private String code;
    private String name;
    private String description;

    private Permission() {
    }

    public static Permission fromPersistence(Long id, String code, String name, String description) {
        Permission permission = new Permission();
        permission.id = id;
        permission.code = code;
        permission.name = name;
        permission.description = description;
        return permission;
    }
}
