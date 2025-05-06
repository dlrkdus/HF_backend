package com.hf.healthfriend.domain.notification.consumer;

import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.Alarm;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.service.NotificationSSEService;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import com.hf.healthfriend.domain.notification.util.NotificationMessageGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlarmWorkerTest {

    @Mock
    private NotificationSSEService notificationSseService;

    @Mock
    private NotificationMessageGenerator messageGenerator;

    @Mock
    private JsonUtils jsonUtils;

    @InjectMocks
    private AlarmWorker alarmWorker;

    @Test
    void testConsumeAlarmMessage_Success() {
        // Given
        String message = "{\"Message\":\"{\\\"memberId\\\":1,\\\"type\\\":\\\"ADD_COMMENT_TO_POST\\\",\\\"actor\\\":\\\"testUser\\\",\\\"targetId\\\":100}\"}";
        NotificationEvent event = new NotificationEvent(1L, NotificationType.ADD_COMMENT_TO_POST, "testUser", 100L);
        Alarm alarm = Alarm.builder()
                .event(event)
                .alarmMessage("Sse Worker Test")
                .build();

        Mockito.when(jsonUtils.deserializeMessage(message)).thenReturn(event);
        Mockito.when(messageGenerator.generateMessage(event)).thenReturn("Sse Worker Test");

        // When
        alarmWorker.consumeAlarmMessage(message);

        // Then
        Mockito.verify(notificationSseService, Mockito.times(1)).send(1L, alarm);
    }
}