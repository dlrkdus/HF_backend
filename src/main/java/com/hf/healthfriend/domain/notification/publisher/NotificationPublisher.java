package com.hf.healthfriend.domain.notification.publisher;

import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.service.NotificationService;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final SqsAsyncClient sqsAsyncClient;
    private final JsonUtils jsonUtils;
    @Value("${aws.sqs.alarmQueueUrl}")
    private String alarmQueueUrl;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishNotification(NotificationEvent event) {
        String message = jsonUtils.serialize(event);
        sendMessage(event, message);
    }

    private void sendMessage(NotificationEvent event, String message) {
        sqsAsyncClient.sendMessage(builder -> builder
                        .queueUrl(alarmQueueUrl)
                        .messageBody(message)
                )
                .orTimeout(3, TimeUnit.SECONDS) // 3초 타임아웃
                .whenComplete((res, throwable) -> {
                    if (throwable != null) {
                        notificationService.updateMessageStatusIfNotSuccess(event.getNotificationId(), MessageStatus.FAILED);
                        if (throwable instanceof SdkClientException){
                            log.warn("SQS 전송 실패(타임아웃) attempt={}", throwable.getMessage());
                        }else{
                            log.warn("SQS 전송 실패 attempt={}", throwable.getMessage());
                        }
                    } else {
                        notificationService.updateMessageStatus(event.getNotificationId(), MessageStatus.SUCCESS);
                        log.info("SQS 전송 성공: messageId={}, notificationId={}",
                                res.messageId(), event.getNotificationId());
                    }
                });
    }
}