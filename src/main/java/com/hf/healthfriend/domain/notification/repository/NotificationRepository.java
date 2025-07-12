package com.hf.healthfriend.domain.notification.repository;

import com.hf.healthfriend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Query(value = "INSERT IGNORE INTO notification (member_id, type, target_id, message, is_read)"
            + "VALUE (:memberId, :type, :targetId, :message, false)", nativeQuery = true)
    void saveAlarm(@Param("memberId") Long memberId, @Param("type") String type,
                   @Param("targetId") Long targetId, @Param("message") String message);
}