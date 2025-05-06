package com.hf.healthfriend.domain.chat.repository;

import com.hf.healthfriend.domain.chat.entity.ChatParticipation;
import com.hf.healthfriend.domain.chat.entity.ChatParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, ChatParticipationId> {

    @Query("""
            SELECT cp.member.id
            FROM ChatParticipation cp
            WHERE cp.chatroom.chatroomId = :chatroomId
            """)
    List<Long> findParticipantIdsByChatroomId(@Param("chatroomId") Long chatroomId);
}
