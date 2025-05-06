package com.hf.healthfriend.global.concurrency.synchronizedexecutor;

import com.hf.healthfriend.global.concurrency.SynchronizedExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MethodSynchronizedExecutor implements SynchronizedExecutor {
    private final PlatformTransactionManager txManager;

    @Override
    public synchronized Object executeWithLock(Supplier<Object> targetLogic) throws Throwable {
        TransactionStatus txStatus = this.txManager.getTransaction(new DefaultTransactionAttribute());

        try {
            Object returnValue = targetLogic.get();
            this.txManager.commit(txStatus);
            return returnValue;
        } catch (Exception e) {
            this.txManager.rollback(txStatus);
            throw e;
        }
    }
}
