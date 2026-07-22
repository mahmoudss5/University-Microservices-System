package com.uni.iam.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Calculates execution time for methods annotated with {@link ExecutionTime}.
 */
@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("@annotation(com.uni.iam.aop.ExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedNanos = System.nanoTime() - startedAt;
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
            log.info("[AOP] {} executed in {} ms",
                    joinPoint.getSignature().toShortString(), elapsedMillis);
        }
    }
}
