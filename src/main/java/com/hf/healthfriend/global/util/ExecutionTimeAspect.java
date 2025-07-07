package com.hf.healthfriend.global.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    @Around("@annotation(com.hf.healthfriend.global.util.ExecutionTime)")// 메서드 실행 전후 수행 작업 정의
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed(); // 실제 타겟 메서드를 실행한다.
        long durationTime = System.currentTimeMillis()- startTime;
        // joinPoint.getSignature()를 통해 타겟 메서드의 메서드 시그니처를 가져올 수 있습니다.
        // 시그니처에는 메서드의 이름, 반환 타입, 매개변수 타입 등의 정보가 포함됩니다.
        String methodName = joinPoint.getSignature().getName();
        log.info("[실행 시간] {}에 {}ms가 소요되었습니다.", methodName, durationTime);
        return result;
    }
}
