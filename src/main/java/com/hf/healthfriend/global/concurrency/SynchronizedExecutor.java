package com.hf.healthfriend.global.concurrency;

import java.util.function.Supplier;

/**
 * 동시성 문제를 해결하기 위해 Lock을 사용하여 비즈니스 로직 메소드 처리
 */
public interface SynchronizedExecutor {

    Object executeWithLock(Supplier<Object> targetLogic) throws Throwable;
}
