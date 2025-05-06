package com.hf.healthfriend.domain.notification.repository;

import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationListResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationCustomRepository {
    NotificationListResponse getList(NotificationType notificationType, Pageable pageable);
}
