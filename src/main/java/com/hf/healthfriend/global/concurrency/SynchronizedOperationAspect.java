package com.hf.healthfriend.global.concurrency;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class SynchronizedOperationAspect {
    private final SynchronizedExecutor executor;
    @Around("@annotation(com.hf.healthfriend.global.concurrency.SynchronizedOperation)")
    public Object aroundLogic(ProceedingJoinPoint joinPoint) throws Throwable {
        return this.executor.executeWithLock(() -> {
            try {
                return joinPoint.proceed(joinPoint.getArgs());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
