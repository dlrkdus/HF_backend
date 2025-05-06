package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.notification.constant.NotificationGetType;
import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationListResponse;
import com.hf.healthfriend.domain.notification.dto.NotificationResponse;
import com.hf.healthfriend.domain.notification.repository.NotificationCustomRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationCustomRepository notificationCustomRepository;

    public NotificationListResponse getList(int page, int size, NotificationType notificationType) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return notificationCustomRepository.getList(notificationType,pageable);
    }
}
