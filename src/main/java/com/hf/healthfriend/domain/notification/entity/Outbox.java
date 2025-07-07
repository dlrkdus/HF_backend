package com.hf.healthfriend.domain.notification.entity;

import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.constant.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String payload;

    private int tryCount;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    public void updateStatus(MessageStatus status) {
        this.status = status;
    }

    public void increaseTryCount() {
        this.tryCount++;
    }

}
