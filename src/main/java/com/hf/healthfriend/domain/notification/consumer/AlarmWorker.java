package com.hf.healthfriend.domain.notification.consumer;

import com.hf.healthfriend.domain.notification.dto.Alarm;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.entity.Notification;
import com.hf.healthfriend.domain.notification.repository.NotificationRepository;
import com.hf.healthfriend.domain.notification.service.NotificationSSEService;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import com.hf.healthfriend.domain.notification.util.NotificationMessageGenerator;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmWorker {

    private final NotificationSSEService notificationSseService;
    private final NotificationRepository notificationRepository;
    private final NotificationMessageGenerator messageGenerator;
    private final JsonUtils jsonUtils;

    @SqsListener("${aws.sqs.alarmQueueUrl}")
    @Transactional
    public void consumeAlarmMessage(String message) {
        try {
            NotificationEvent event = jsonUtils.deserializeMessage(message);
            String alarmMessage = messageGenerator.generateMessage(event);
            Long memberId = event.memberId();

            // 1. SSE 알림 전송
            Alarm alarm = Alarm.builder()
                    .event(event)
                    .alarmMessage(alarmMessage)
                    .build();
            notificationSseService.send(memberId, alarm);
            log.info("SSE 알림 전송 완료: {}", event);

            // 2. DB 저장
            Notification notification = Notification.builder()
                    .memberId(event.memberId())
                    .type(event.type())
                    .targetId(event.targetId())
                    .message(alarmMessage)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.info("DB 저장 완료: {}", event);

        } catch (Exception e) {
            log.error("알림 처리 실패: 메시지={}, 에러={}", message, e.getMessage(), e);
            throw e; // SQS 자동 재시도를 위해 예외 던지기
        }
    }
}