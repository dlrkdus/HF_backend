package com.hf.healthfriend.domain.notification.repository;

import com.hf.healthfriend.domain.notification.constant.MessageStatus;
import com.hf.healthfriend.domain.notification.entity.Outbox;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    @Modifying
    @Query("UPDATE Outbox o SET o.status = :messageStatus WHERE o.notificationId = :notificationId")
    void updateMessageStatus(@Param("notificationId") String notificationId, @Param("messageStatus") MessageStatus messageStatus);

    @Query("UPDATE Outbox o SET o.status = :messageStatus WHERE o.notificationId = :notificationId AND o.status <> 'SUCCESS'")
    void updateMessageStatusIfNotSuccess(@Param("notificationId") String notificationId, @Param("messageStatus") MessageStatus messageStatus);

    @Query("SELECT o FROM Outbox o WHERE o.status = :s1 OR o.status = :s2")
    List<Outbox> findUnSentMessages(@Param("s1") MessageStatus s1, @Param("s2") MessageStatus s2);
}
