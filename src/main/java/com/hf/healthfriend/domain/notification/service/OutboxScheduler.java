package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.entity.Outbox;
import com.hf.healthfriend.domain.notification.repository.OutboxRepository;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    // TODO : 이걸 아웃박스 테이블에서 꺼내도록 변경
    private final SqsClient sqsClient;
    private final JsonUtils jsonUtils;
    private final OutboxRepository outboxRepository;
    @Value("${aws.sqs.alarmQueueUrl}")
    private String alarmQueueUrl;

    @Scheduled(fixedDelay = 60000) // 1분마다
    public void retryFailedNotifications() {
        // FAILED 또는 PENDING 메세지 읽어오기
        List<Outbox> unsentMessages = outboxRepository.findUnSentMessages(MessageStatus.FAILED, MessageStatus.PENDING);
        if (unsentMessages.isEmpty()) {
            log.info("No unsent messages found");
            return; }
        for (Outbox message : unsentMessages) {
            message.increaseTryCount();
            try {
                String payload = jsonUtils.serialize(message.getPayload());
                sqsClient.sendMessage(builder -> builder
                        .queueUrl(alarmQueueUrl)
                        .messageBody(payload)
                );
                outboxRepository.updateMessageStatus(message.getNotificationId(), MessageStatus.SUCCESS);
                log.info("SQS 전송 성공: messageId={}, notificationId={}, tryCount ={}",
                        message.getId(), message.getNotificationId(), message.getTryCount());
            }
            catch (Exception e) {
                outboxRepository.updateMessageStatusIfNotSuccess(message.getNotificationId(), MessageStatus.FAILED);
                if (e instanceof SdkClientException){
                    log.warn("SQS 전송 실패(타임아웃) attempt={}", e.getMessage());
                }else{
                    log.warn("SQS 전송 실패 attempt={}", e.getMessage());
                }
            }finally {
                if (message.getTryCount() >= 3 && message.getStatus() != MessageStatus.SUCCESS) {
                    message.updateStatus(MessageStatus.EXCLUDED);
                    log.error("SQS 메시지 전송 최종 실패 및 EXCLUDED 처리: messageId={}, notificationId={}",
                            message.getId(), message.getNotificationId());
                }
            }
        }
    }
}