package cn.tx.aiagent.common.security;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.config.LoginContextHolder;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.login.domain.LoginContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限校验 AOP 切面。
 * 拦截标注了 @RequirePermission 的 Controller 方法，校验当前用户权限。
 */
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(cn.tx.aiagent.common.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequirePermission annotation = signature.getMethod().getAnnotation(RequirePermission.class);

        if (annotation == null) {
            return joinPoint.proceed();
        }

        String required = annotation.value();
        LoginContext ctx = LoginContextHolder.get();

        if (ctx == null) {
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
        }

        List<String> permissions = ctx.permissions();
        if (!permissions.contains(required)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return joinPoint.proceed();
    }
}
