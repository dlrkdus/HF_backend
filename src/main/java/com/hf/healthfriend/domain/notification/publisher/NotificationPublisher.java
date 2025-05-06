package com.hf.healthfriend.domain.notification.publisher;

import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final SqsAsyncClient sqsAsyncClient;
    private final JsonUtils jsonUtils;

    @Value("${aws.sqs.alarmQueueUrl}")
    private String alarmQueueUrl;

    public void publishNotification(Long memberId, NotificationType type, String actor, Long targetId) {
        NotificationEvent event = new NotificationEvent(memberId, type, actor, targetId);
        String message = jsonUtils.serialize(event);

        // 트랜잭션이 커밋된 이후에 실행되도록 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sqsAsyncClient.sendMessage(builder -> builder
                        .queueUrl(alarmQueueUrl)
                        .messageBody(message)
                );

                log.info("SQS에 알림 이벤트 퍼블리싱 (트랜잭션 커밋 후): memberId={}, type={}, actor={}, targetId={}",
                        memberId, type, actor, targetId);
            }
        });
    }
}

