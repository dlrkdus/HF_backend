package com.hf.healthfriend.domain.notification.service;

import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.constant.NotificationType;
import com.hf.healthfriend.domain.notification.dto.NotificationEvent;
import com.hf.healthfriend.domain.notification.dto.NotificationListResponse;
import com.hf.healthfriend.domain.notification.repository.NotificationCustomRepository;
import com.hf.healthfriend.domain.notification.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationCustomRepository notificationCustomRepository;
    private final OutboxRepository outboxRepository;

    public NotificationListResponse getList(int page, int size, NotificationType notificationType) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return notificationCustomRepository.getList(notificationType,pageable);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMessageStatus(String notificationId, MessageStatus status) {
        outboxRepository.updateMessageStatus(notificationId, status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMessageStatusIfNotSuccess(String notificationId, MessageStatus messageStatus) {
        outboxRepository.updateMessageStatusIfNotSuccess(notificationId, messageStatus);
    }
}
