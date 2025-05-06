package com.hf.healthfriend.domain.notification.consumer;

import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.entity.Notification;
import com.hf.healthfriend.domain.notification.repository.NotificationRepository;
import com.hf.healthfriend.domain.notification.util.JsonUtils;
import com.hf.healthfriend.domain.notification.util.NotificationMessageGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class DBWorkerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMessageGenerator messageGenerator;

    @Mock
    private JsonUtils jsonUtils;

    @InjectMocks
    private DBWorker dbWorker;

    @Test
    void testConsumeDbQueue_Success() {
        // Given
        String message = "{\"Message\":\"{\\\"memberId\\\":1,\\\"type\\\":\\\"ADD_COMMENT_TO_POST\\\",\\\"actor\\\":\\\"testUser\\\",\\\"targetId\\\":100}\"}";
        NotificationEvent event = new NotificationEvent(1L, NotificationType.ADD_COMMENT_TO_POST, "testUser", 100L);
        Mockito.when(jsonUtils.deserializeMessage(message)).thenReturn(event);
        Mockito.when(messageGenerator.generateMessage(event)).thenReturn("DB Worker Test");

        // When
        dbWorker.consumeDbQueue(message);

        // Then
        Mockito.verify(notificationRepository, Mockito.times(1)).save(Mockito.any(Notification.class));
    }
}