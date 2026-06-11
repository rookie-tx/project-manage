package cn.tx.aiagent.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级权限校验注解。
 * 标记在 Controller 方法上，声明调用该接口所需的权限。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /** 所需权限 code，如 "user:delete" */
    String value();
}
