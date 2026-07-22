package com.uni.iam.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
public class GeneralLoggingAspect {

    private static final String GENERAL_LOG_POINTCUT = "@annotation(com.uni.iam.aop.GeneralLog)";

    @Before(GENERAL_LOG_POINTCUT)
    public void logMethodEntry(JoinPoint joinPoint) {
        log.info("[AOP] Calling: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(GENERAL_LOG_POINTCUT)
    public void logMethodSuccess(JoinPoint joinPoint) {
        log.info("[AOP] Completed: {}", joinPoint.getSignature().toShortString());
    }

    @AfterThrowing(pointcut = GENERAL_LOG_POINTCUT, throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        log.error("[AOP] Exception in: {} | {}: {}",
                joinPoint.getSignature().toShortString(),
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }
}
