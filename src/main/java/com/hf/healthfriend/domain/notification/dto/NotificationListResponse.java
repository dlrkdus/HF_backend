package com.hf.healthfriend.domain.notification.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record NotificationListResponse(
        int totalPageSize,
        List<NotificationResponse> notificationResponseList
) {
}
