package com.hf.healthfriend.domain.chat.repository;

import com.hf.healthfriend.domain.chat.entity.chatmessage.ChatMessage;
import com.hf.healthfriend.domain.chat.repository.custom.ChatMessageCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageCustomRepository {

    @Query("""
            SELECT cm
            FROM ChatMessage cm
            INNER JOIN Chatroom cr ON cr.chatroomId = cm.chatroom.chatroomId
            WHERE cr.chatroomId = :chatroomId
            ORDER BY cm.creationTime DESC
            """)
    Page<ChatMessage> findByChatroomId(@Param("chatroomId") Long chatroomId, Pageable page);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ChatMessage
            SET readByOpponent = TRUE
            WHERE chatroom.chatroomId = :chatroomId
            """)
    void readMessagesInChatroomByOpponent(@Param("chatroomId") Long chatroomId);
}
