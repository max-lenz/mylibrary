package com.mylibrary.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    private static final Logger PERF = LoggerFactory.getLogger("PERFORMANCE");

    @Around("within(com.mylibrary.service..*)")
    public Object logServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        long start = System.nanoTime();
        try {
            Object result = pjp.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            PERF.info("service={} method={} duration={}ms status=success",
                    className, methodName, durationMs);
            return result;
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.error("{}.{} failed after {} ms: {}",
                    className, methodName, durationMs, ex.getMessage(), ex);
            PERF.info("service={} method={} duration={}ms status=failed error={}",
                    className, methodName, durationMs, ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
