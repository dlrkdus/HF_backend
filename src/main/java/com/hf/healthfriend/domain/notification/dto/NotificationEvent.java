package com.hf.healthfriend.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hf.healthfriend.domain.notification.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {
        private String notificationId;
        private Long memberId;
        private NotificationType type;
        private String actor;
        private Long targetId;
}