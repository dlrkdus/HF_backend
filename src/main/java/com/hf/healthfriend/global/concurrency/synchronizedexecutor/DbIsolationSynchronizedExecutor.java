package com.hf.healthfriend.global.concurrency.synchronizedexecutor;

import com.hf.healthfriend.global.concurrency.SynchronizedExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
@Primary
@RequiredArgsConstructor
public class DbIsolationSynchronizedExecutor implements SynchronizedExecutor {

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public Object executeWithLock(Supplier<Object> targetLogic) throws Throwable {
        return targetLogic.get();
    }
}
