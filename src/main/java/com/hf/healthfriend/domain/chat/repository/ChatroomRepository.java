package com.hf.healthfriend.domain.chat.repository;

import com.hf.healthfriend.domain.chat.entity.Chatroom;
import com.hf.healthfriend.domain.chat.repository.custom.ChatroomCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long>, ChatroomCustomRepository {
}
