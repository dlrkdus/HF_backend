package com.hf.healthfriend.domain.notification.dto;

import com.hf.healthfriend.domain.notification.constant.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record NotificationResponse(
        String message,
        NotificationType type,
        LocalDateTime time,
        Long targetId
) {
}
