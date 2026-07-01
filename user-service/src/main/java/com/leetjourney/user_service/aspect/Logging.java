package com.leetjourney.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class Logging {
    @Pointcut("execution(* com.leetjourney.user_service.service.*.*(..))")

    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Executing method: {}  with arguments: {}"
                , joinPoint.getSignature().toShortString(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "serviceMethods()",returning = "result")
    public void logAfterReturning(JoinPoint joinPoint ,Object result) {
           log.info("Service Method {} returned: {}", joinPoint.getSignature().getName(), result);
    }

}
